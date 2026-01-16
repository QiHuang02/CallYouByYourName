package cn.qihuang02.callyou.core.mention.executor;

import cn.qihuang02.callyou.api.MentionContext;
import cn.qihuang02.callyou.api.TargetCollection;
import cn.qihuang02.callyou.config.CallYouConfig;
import cn.qihuang02.callyou.core.saveddata.MentionRecord;
import cn.qihuang02.callyou.core.saveddata.MentionSavedData;
import cn.qihuang02.callyou.core.mention.executor.MentionResult.MentionStatus;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class MentionHistoryRecorder {
    public void record(
            @NotNull MentionStatus status,
            @NotNull MentionContext context,
            @NotNull TargetCollection targets,
            @NotNull List<ServerPlayer> onlineTargets,
            @NotNull List<ServerPlayer> readTargets,
            @NotNull Component formattedMessage
    ) {
        if (status == MentionStatus.TARGETLESS_ALLOWED) {
            return;
        }
        if (!CallYouConfig.COMMON.enableServerSideHistory.get()) {
            return;
        }
        List<UUID> onlineTargetIds = onlineTargets.stream().map(ServerPlayer::getUUID).toList();
        List<UUID> readTargetIds = readTargets.stream().map(ServerPlayer::getUUID).toList();
        List<UUID> offlineTargetIds = collectOfflineTargets(targets, onlineTargetIds, context.senderId());
        if (readTargetIds.isEmpty() && offlineTargetIds.isEmpty()) {
            return;
        }
        MentionSavedData savedData = MentionSavedData.get(context.level());
        Set<UUID> uniqueReadTargets = new LinkedHashSet<>(readTargetIds);
        for (UUID targetId : uniqueReadTargets) {
            MentionRecord record = buildMentionRecord(
                    context,
                    List.of(targetId),
                    List.of(targetId),
                    formattedMessage
            );
            savedData.addLog(record);
        }
        for (UUID targetId : offlineTargetIds) {
            MentionRecord record = buildMentionRecord(
                    context,
                    List.of(targetId),
                    List.of(),
                    formattedMessage
            );
            savedData.addLog(record);
        }
    }

    private @NotNull MentionRecord buildMentionRecord(
            @NotNull MentionContext context,
            @NotNull List<UUID> targetIds,
            @NotNull List<UUID> readTargets,
            @NotNull Component formattedMessage
    ) {
        Component messageCopy = formattedMessage.copy();
        return MentionRecord.create(
                context.senderId(),
                context.senderName(),
                messageCopy,
                System.currentTimeMillis(),
                targetIds,
                readTargets
        );
    }

    private @NotNull List<UUID> collectOfflineTargets(
            @NotNull TargetCollection targets,
            @NotNull List<UUID> onlineTargetIds,
            @NotNull UUID senderId
    ) {
        if (targets.isEmpty()) {
            return List.of();
        }
        Set<UUID> onlineTargets = new LinkedHashSet<>(onlineTargetIds);
        Set<UUID> unique = new LinkedHashSet<>();
        for (UUID id : targets.allIds()) {
            if (id == null || id.equals(senderId) || onlineTargets.contains(id)) {
                continue;
            }
            unique.add(id);
        }
        return List.copyOf(unique);
    }
}
