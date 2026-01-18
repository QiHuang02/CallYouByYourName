package cn.qihuang02.callyou.core.mention.lifecycle;

import cn.qihuang02.callyou.api.MentionCandidate;
import cn.qihuang02.callyou.api.MentionContext;
import cn.qihuang02.callyou.config.CallYouConfig;
import cn.qihuang02.callyou.core.saveddata.MentionRecord;
import cn.qihuang02.callyou.core.saveddata.MentionSavedData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class MentionHistoryRecorder implements MentionLifeCycle {
    @Override
    public void process(@NotNull MentionContext context) {
        if (!CallYouConfig.COMMON.enableServerSideHistory.get()) {
            return;
        }
        if (context.getSuccessfulCandidates().isEmpty()) {
            return;
        }

        Component message = context.finalMessage() == null
                ? context.originalMessage()
                : context.finalMessage();

        Set<UUID> readTargets = new HashSet<>();
        for (ServerPlayer target : context.notifiedTargets()) {
            readTargets.add(target.getUUID());
        }

        MentionSavedData savedData = MentionSavedData.get(context.level());

        for (MentionCandidate candidate : context.getSuccessfulCandidates()) {
            if (candidate.resolvedTargets().isEmpty()) {
                continue;
            }
            for (UUID targetId : candidate.resolvedTargets()) {
                boolean read = readTargets.contains(targetId);
                MentionRecord record = MentionRecord.create(
                        context.senderId(),
                        context.senderName(),
                        message.copy(),
                        System.currentTimeMillis(),
                        targetId,
                        read,
                        candidate.resolveStatus(),
                        candidate.key()
                );
                savedData.addLog(record);
            }
        }
    }
}
