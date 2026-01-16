package cn.qihuang02.callyou.core;

import cn.qihuang02.callyou.core.attachment.CallYouAttachments;
import cn.qihuang02.callyou.core.attachment.MentionPreferences;
import cn.qihuang02.callyou.CallYouByYourName;
import cn.qihuang02.callyou.core.handler.OnlinePlayersHandler;
import cn.qihuang02.callyou.core.mention.executor.MentionDispatcher;
import cn.qihuang02.callyou.core.mention.executor.MentionResult.MentionEntry;
import cn.qihuang02.callyou.core.mention.executor.MentionResult.MentionStatus;
import cn.qihuang02.callyou.core.mention.executor.MentionResult.MessageMentionOutcome;
import cn.qihuang02.callyou.core.mention.executor.MentionResult.MessageResult;
import cn.qihuang02.callyou.core.mention.executor.MentionResult.PermissionResult;
import cn.qihuang02.callyou.core.mention.executor.MentionHistoryRecorder;
import cn.qihuang02.callyou.core.mention.executor.MentionMessageComposer;
import cn.qihuang02.callyou.core.mention.executor.MentionPermissionValidator;
import cn.qihuang02.callyou.core.saveddata.MentionPreferencesSavedData;
import cn.qihuang02.callyou.core.mention.components.targetProvider.NoneTargetProvider;
import cn.qihuang02.callyou.util.OfflinePlayerList;
import cn.qihuang02.callyou.api.MentionContext;
import cn.qihuang02.callyou.api.MentionType;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.ServerChatEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class MentionExecutor {
    private static final MentionGuard GUARD = new MentionGuard();
    private static final MentionExecutionTools TOOLS = MentionExecutionTools.create(GUARD);

    public static void handlePlayerLogout(@NotNull ServerPlayer player) {
        GUARD.onPlayerLogout(player);
    }

    public static void handleChatEvent(@NotNull ServerChatEvent event) {
        ServerPlayer sender = event.getPlayer();

        try {
            String raw = event.getRawText();
            Component originalMessage = event.getMessage();

            List<MentionResolver.ResolvedMention> mentions =
                    MentionResolver.resolve(sender.server, sender, raw);
            if (mentions.isEmpty()) {
                return;
            }

            MessageResult processing = evaluateMentions(sender, originalMessage, raw, mentions);
            if (processing.failureMessage() != null) {
                sender.sendSystemMessage(processing.failureMessage());
            }
            if (processing.outcome() == MessageMentionOutcome.CANCEL_ALL) {
                event.setCanceled(true);
                return;
            }

            List<MentionEntry> entries = processing.entries();
            if (!hasAllowedMentions(entries)) {
                return;
            }

            PermissionResult permissionResult =
                    TOOLS.permissionValidator().applyPermissions(sender, entries);
            if (permissionResult.errorMessage() != null) {
                sender.sendSystemMessage(permissionResult.errorMessage());
                event.setCanceled(true);
                return;
            }

            int effectiveMentionCount = permissionResult.effectiveMentionCount();
            if (effectiveMentionCount <= 0) {
                return;
            }

            long nowTick = sender.server.getTickCount();

            if (!GUARD.checkMessageRate(sender, effectiveMentionCount, nowTick)) {
                sender.sendSystemMessage(
                        Component.translatable("message.callyou.too_many_mentions")
                );
                event.setCanceled(true);
                return;
            }

            MentionMessageComposer.ComposeResult composeResult =
                    TOOLS.messageComposer().compose(sender, originalMessage, raw, permissionResult.entries());
            event.setMessage(composeResult.rebuilt());

            List<ServerPlayer> allTargetsHit = TOOLS.dispatcher()
                    .dispatchAll(composeResult.pendingMentions(), nowTick, composeResult.rebuilt());
            GUARD.recordUsage(sender, allTargetsHit, nowTick);

            if (!allTargetsHit.isEmpty()) {
                event.setCanceled(true);
                sendChatToTargets(
                        sender,
                        composeResult.rebuilt(),
                        composeResult.senderView(),
                        allTargetsHit
                );
            }
        } catch (MentionCancelException cancel) {
            sender.sendSystemMessage(cancel.getReason());
            event.setCanceled(true);
        }
    }

    private static void sendChatToTargets(
            @NotNull ServerPlayer sender,
            @NotNull Component message,
            @NotNull Component senderMessage,
            @NotNull List<ServerPlayer> targets
    ) {
        Set<ServerPlayer> recipients = new LinkedHashSet<>(targets.size() + 1);
        recipients.addAll(targets);
        // Ensure the sender still sees their own message.
        recipients.add(sender);

        ChatType.Bound boundType = ChatType.bind(ChatType.CHAT, sender);
        for (ServerPlayer recipient : recipients) {
            Component selected = recipient == sender ? senderMessage : message;
            OutgoingChatMessage outgoing = new OutgoingChatMessage.Disguised(selected);
            recipient.sendChatMessage(outgoing, sender.shouldFilterMessageTo(recipient), boundType);
        }
    }

    private static @NotNull MessageResult evaluateMentions(
            @NotNull ServerPlayer sender,
            @NotNull Component originalMessage,
            @NotNull String raw,
            @NotNull List<MentionResolver.ResolvedMention> mentions
    ) {
        List<MentionEntry> entries = new ArrayList<>(mentions.size());
        Set<String> blockedNames = new LinkedHashSet<>();
        UUID senderId = sender.getUUID();
        ResourceLocation playerMentionId = CallYouByYourName.getRl("player");
        OfflinePlayerList offlinePlayers = OnlinePlayersHandler.getOfflinePlayers();
        MentionPreferencesSavedData cachedPrefs = MentionPreferencesSavedData.get(sender.serverLevel());
        int allowedCount = 0;
        int failedCount = 0;

        for (MentionResolver.ResolvedMention mention : mentions) {
            MentionType type = mention.mentionType();
            MentionStatus status = decideMention(
                    sender,
                    mention,
                    playerMentionId,
                    offlinePlayers,
                    cachedPrefs,
                    senderId
            );

            if (status == MentionStatus.ALLOWED
                    && type != null
                    && !(type.targetProvider() instanceof NoneTargetProvider)) {
                MentionContext context = new MentionContext(
                        sender,
                        originalMessage,
                        raw,
                        mention.key(),
                        mention.typeId()
                );
                if (type.targetProvider().resolveTargets(context).isEmpty()) {
                    status = MentionStatus.NO_TARGETS;
                }
            }

            if (status.isAllowed()) {
                allowedCount++;
            }
            if (status.countsAsFailure()) {
                failedCount++;
            }
            if (status.shouldCollectBlockedName()) {
                String blockedName = resolveBlockedName(mention);
                if (blockedName != null && !blockedName.isEmpty()) {
                    blockedNames.add(blockedName);
                }
            }

            entries.add(new MentionEntry(mention, status));
        }

        MessageMentionOutcome outcome;
        if (allowedCount == 0) {
            outcome = failedCount == 0 ? MessageMentionOutcome.ALLOW_ALL : MessageMentionOutcome.CANCEL_ALL;
        } else if (failedCount > 0) {
            outcome = MessageMentionOutcome.STRIP_SOME;
        } else {
            outcome = MessageMentionOutcome.ALLOW_ALL;
        }

        Component failureMessage = null;
        if (!blockedNames.isEmpty()) {
            String joined = String.join(", ", blockedNames);
            failureMessage = Component.translatable(
                    "message.callyou.mention.disallowed",
                    Component.literal(joined)
            );
        }

        return new MessageResult(entries, outcome, failureMessage);
    }

    private static @NotNull MentionStatus decideMention(
            @NotNull ServerPlayer sender,
            @NotNull MentionResolver.ResolvedMention mention,
            @NotNull ResourceLocation playerMentionId,
            @NotNull OfflinePlayerList offlinePlayers,
            @NotNull MentionPreferencesSavedData cachedPrefs,
            @NotNull UUID senderId
    ) {
        MentionType type = mention.mentionType();
        if (type == null) {
            return MentionStatus.NOT_FOUND;
        }
        if (type.targetProvider() instanceof NoneTargetProvider) {
            return MentionStatus.TARGETLESS_ALLOWED;
        }
        if (mention.typeId() != null && mention.typeId().equals(playerMentionId)) {
            ServerPlayer target = mention.playerTarget();
            if (target != null) {
                MentionPreferences prefs = target.getData(CallYouAttachments.MENTION_PREFERENCES.get());
                return decideByPreferences(prefs, type, mention.typeId(), senderId);
            }
            UUID targetId = offlinePlayers.findPlayerByExactName(sender.server, mention.key());
            if (targetId != null && !targetId.equals(senderId)) {
                return cachedPrefs.find(targetId)
                        .map(prefs -> decideByPreferences(prefs, type, mention.typeId(), senderId))
                        .orElse(MentionStatus.ALLOWED);
            }
        }
        return MentionStatus.ALLOWED;
    }

    private static @NotNull MentionStatus decideByPreferences(
            @NotNull MentionPreferences prefs,
            @NotNull MentionType type,
            @NotNull ResourceLocation typeId,
            @NotNull UUID senderId
    ) {
        if (!prefs.isAllowMentions()) {
            return MentionStatus.BLOCKED_PREFS;
        }
        if (prefs.getBlockedSenders().contains(senderId)) {
            return MentionStatus.BLOCKED_BLACKLIST;
        }
        if (prefs.getBlockedTypes().contains(typeId)) {
            return MentionStatus.BLOCKED_TYPE;
        }
        if (!prefs.isAllowMassMentions() && type.rules() != null && type.rules().isMass()) {
            return MentionStatus.BLOCKED_MASS;
        }
        return MentionStatus.ALLOWED;
    }

    private static String resolveBlockedName(@NotNull MentionResolver.ResolvedMention mention) {
        ServerPlayer target = mention.playerTarget();
        if (target != null) {
            return target.getDisplayName().getString();
        }
        return mention.key();
    }

    private static boolean hasAllowedMentions(@NotNull List<MentionEntry> entries) {
        for (MentionEntry entry : entries) {
            if (entry.status().isAllowed()) {
                return true;
            }
        }
        return false;
    }

    private record MentionExecutionTools(
            @NotNull MentionPermissionValidator permissionValidator,
            @NotNull MentionMessageComposer messageComposer,
            @NotNull MentionDispatcher dispatcher
    ) {
        private static @NotNull MentionExecutionTools create(@NotNull MentionGuard guard) {
            MentionHistoryRecorder historyRecorder = new MentionHistoryRecorder();
            return new MentionExecutionTools(
                    new MentionPermissionValidator(guard),
                    new MentionMessageComposer(),
                    new MentionDispatcher(guard, historyRecorder)
            );
        }
    }
}
