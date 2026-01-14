package cn.qihuang02.callyou.core;

import cn.qihuang02.callyou.core.mention.executor.MentionDispatcher;
import cn.qihuang02.callyou.core.mention.executor.MentionHistoryRecorder;
import cn.qihuang02.callyou.core.mention.executor.MentionMessageComposer;
import cn.qihuang02.callyou.core.mention.executor.MentionPermissionValidator;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.ServerChatEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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

            MentionPermissionValidator.ValidationResult validation =
                    TOOLS.permissionValidator().validate(sender, mentions);
            if (validation.errorMessage() != null) {
                sender.sendSystemMessage(validation.errorMessage());
                event.setCanceled(true);
                return;
            }

            int effectiveMentionCount = validation.effectiveMentionCount();
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
                    TOOLS.messageComposer().compose(sender, originalMessage, raw, mentions);
            event.setMessage(composeResult.rebuilt());

            List<ServerPlayer> allTargetsHit = TOOLS.dispatcher()
                    .dispatchAll(composeResult.pendingMentions(), nowTick, composeResult.rebuilt());
            GUARD.recordUsage(sender, allTargetsHit, nowTick);
        } catch (MentionCancelException cancel) {
            sender.sendSystemMessage(cancel.getReason());
            event.setCanceled(true);
        }
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
