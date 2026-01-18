package cn.qihuang02.callyou.core;

import cn.qihuang02.callyou.api.DeliveryStatus;
import cn.qihuang02.callyou.api.MentionCandidate;
import cn.qihuang02.callyou.api.MentionContext;
import cn.qihuang02.callyou.api.ResolveStatus;
import cn.qihuang02.callyou.core.attachment.CallYouAttachments;
import cn.qihuang02.callyou.core.mention.lifecycle.*;
import cn.qihuang02.callyou.core.saveddata.MentionPreferencesSavedData;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.ServerChatEvent;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class MentionExecutor {
    private static final MentionGuard GUARD = new MentionGuard();
    private static final MentionExecutionTools TOOLS = MentionExecutionTools.create();

    public static void handlePlayerLogout(@NotNull ServerPlayer player) {
        GUARD.onPlayerLogout(player);
        MentionPreferencesSavedData.get(player.serverLevel())
                .updatePreferences(player.getUUID(), player.getData(CallYouAttachments.MENTION_PREFERENCES.get()));
    }

    public static void handleChatEvent(@NotNull ServerChatEvent event) {
        ServerPlayer sender = event.getPlayer();

        String raw = event.getRawText();
        Component originalMessage = event.getMessage();

        MentionContext context = new MentionContext(sender, originalMessage, raw);
        MentionResolver resolver = new MentionResolver();
        resolver.process(context);
        if (context.candidates().isEmpty()) {
            return;
        }

        TOOLS.permissionValidator().process(context);
        TOOLS.rateLimiter().process(context);
        TOOLS.messageComposer().process(context);
        TOOLS.dispatcher().process(context);
        TOOLS.historyRecorder().process(context);

        sendCandidateMessages(sender, context);
        if (shouldCancelSingleMention(context)) {
            event.setCanceled(true);
            return;
        }

        Component rebuilt = context.finalMessage();
        if (rebuilt != null) {
            event.setMessage(rebuilt);
        }

        List<ServerPlayer> allTargetsHit = context.notifiedTargets();
        if (!allTargetsHit.isEmpty()) {
            long nowTick = sender.server.getTickCount();
            GUARD.recordUsage(sender, allTargetsHit, nowTick);

            event.setCanceled(true);
            Component senderView = context.senderView() == null
                    ? (rebuilt == null ? originalMessage : rebuilt)
                    : context.senderView();
            sendChatToTargets(sender, rebuilt == null ? originalMessage : rebuilt, senderView, allTargetsHit);
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

    private static void sendCandidateMessages(
            @NotNull ServerPlayer sender,
            @NotNull MentionContext context
    ) {
        for (MentionCandidate candidate : context.candidates()) {
            Component detail = candidate.detailMessage();
            if (detail == null) {
                continue;
            }
            if (candidate.deliveryStatus() == DeliveryStatus.RATE_LIMITED) {
                continue;
            }
            ResolveStatus status = candidate.resolveStatus();
            sender.sendSystemMessage(detail.copy().withStyle(status.getSenderColor()));
        }
    }

    private static boolean shouldCancelSingleMention(@NotNull MentionContext context) {
        MentionCandidate single = null;
        for (MentionCandidate candidate : context.candidates()) {
            if (candidate.type() == null) {
                continue;
            }
            if (single != null) {
                return false;
            }
            single = candidate;
        }
        if (single == null) {
            return false;
        }
        if (single.deliveryStatus() == DeliveryStatus.RATE_LIMITED) {
            return true;
        }
        ResolveStatus status = single.resolveStatus();
        if (status == ResolveStatus.NO_PERMISSION) {
            return true;
        }
        return status == ResolveStatus.NOT_FOUND && single.detailMessage() != null;
    }

    private record MentionExecutionTools(
            @NotNull MentionPermissionValidator permissionValidator,
            @NotNull RateLimiter rateLimiter,
            @NotNull MentionMessageComposer messageComposer,
            @NotNull MentionDispatcher dispatcher,
            @NotNull MentionHistoryRecorder historyRecorder
    ) {
        private static @NotNull MentionExecutionTools create() {
            MentionHistoryRecorder historyRecorder = new MentionHistoryRecorder();
            return new MentionExecutionTools(
                    new MentionPermissionValidator(MentionExecutor.GUARD),
                    new RateLimiter(MentionExecutor.GUARD),
                    new MentionMessageComposer(),
                    new MentionDispatcher(MentionExecutor.GUARD),
                    historyRecorder
            );
        }
    }
}
