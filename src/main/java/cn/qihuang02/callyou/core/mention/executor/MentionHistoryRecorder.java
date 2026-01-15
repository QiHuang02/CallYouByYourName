package cn.qihuang02.callyou.core.mention.executor;

import cn.qihuang02.callyou.api.MentionContext;
import cn.qihuang02.callyou.api.MentionType;
import cn.qihuang02.callyou.config.CallYouConfig;
import cn.qihuang02.callyou.core.saveddata.MentionRecord;
import cn.qihuang02.callyou.core.saveddata.MentionSavedData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class MentionHistoryRecorder {
    public void record(
            @NotNull MentionType type,
            @NotNull MentionContext context,
            @NotNull List<ServerPlayer> targets,
            @NotNull Component formattedMessage
    ) {
        if (!CallYouConfig.COMMON.enableServerSideHistory.get()) {
            return;
        }
        List<UUID> onlineTargetIds = targets.stream().map(ServerPlayer::getUUID).toList();
        List<UUID> offlineTargetIds = collectOfflineTargets(type, context, onlineTargetIds);
        if (onlineTargetIds.isEmpty() && offlineTargetIds.isEmpty()) {
            return;
        }
        MentionSavedData savedData = MentionSavedData.get(context.level());
        for (UUID targetId : onlineTargetIds) {
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
            @NotNull MentionType type,
            @NotNull MentionContext context,
            @NotNull List<UUID> onlineTargetIds
    ) {
        List<UUID> candidates = type.targetProvider().getOfflineTargets(context);
        if (candidates.isEmpty()) {
            return List.of();
        }
        Set<UUID> onlineTargets = new LinkedHashSet<>(onlineTargetIds);
        Set<UUID> unique = new LinkedHashSet<>();
        UUID senderId = context.senderId();
        for (UUID id : candidates) {
            if (id == null || id.equals(senderId) || onlineTargets.contains(id)) {
                continue;
            }
            unique.add(id);
        }
        return List.copyOf(unique);
    }
}
