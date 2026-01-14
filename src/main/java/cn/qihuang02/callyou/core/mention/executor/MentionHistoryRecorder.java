package cn.qihuang02.callyou.core.mention.executor;

import cn.qihuang02.callyou.api.MentionContext;
import cn.qihuang02.callyou.config.CallYouConfig;
import cn.qihuang02.callyou.core.saveddata.MentionRecord;
import cn.qihuang02.callyou.core.saveddata.MentionSavedData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public final class MentionHistoryRecorder {
    public void record(
            @NotNull MentionContext context,
            @NotNull List<ServerPlayer> targets,
            @NotNull Component formattedMessage
    ) {
        if (!CallYouConfig.COMMON.enableServerSideHistory.get()) {
            return;
        }
        MentionSavedData savedData = MentionSavedData.get(context.level());
        List<UUID> targetIds = targets.stream().map(ServerPlayer::getUUID).toList();
        MentionRecord record = buildMentionRecord(context, targetIds, formattedMessage);
        savedData.addLog(record);
    }

    private @NotNull MentionRecord buildMentionRecord(
            @NotNull MentionContext context,
            @NotNull List<UUID> targetIds,
            @NotNull Component formattedMessage
    ) {
        Component messageCopy = formattedMessage.copy();
        List<UUID> readTargets = targetIds;
        return MentionRecord.create(
                context.senderId(),
                context.senderName(),
                messageCopy,
                System.currentTimeMillis(),
                targetIds,
                readTargets
        );
    }
}
