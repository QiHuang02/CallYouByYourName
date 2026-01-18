package cn.qihuang02.callyou.core.mention.lifecycle;

import cn.qihuang02.callyou.api.DeliveryStatus;
import cn.qihuang02.callyou.api.MentionCandidate;
import cn.qihuang02.callyou.api.MentionContext;
import cn.qihuang02.callyou.api.ResolveStatus;
import cn.qihuang02.callyou.core.MentionGuard;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public final class RateLimiter implements MentionLifeCycle {
    private final MentionGuard guard;

    public RateLimiter(@NotNull MentionGuard guard) {
        this.guard = guard;
    }

    @Override
    public void process(@NotNull MentionContext context) {
        ServerPlayer sender = context.sender();
        int effectiveCount = countEffectiveMentions(context);
        if (effectiveCount <= 0) {
            return;
        }
        long nowTick = sender.server.getTickCount();
        if (guard.checkMessageRate(sender, effectiveCount, nowTick)) {
            return;
        }

        Component message = Component.translatable("message.callyou.too_many_mentions")
                .withStyle(DeliveryStatus.RATE_LIMITED.getSenderColor());
        sender.displayClientMessage(message, true);

        for (MentionCandidate candidate : context.candidates()) {
            if (candidate.resolveStatus().isSuccess()) {
                candidate.updateDeliveryStatus(DeliveryStatus.RATE_LIMITED, message);
            }
        }
    }

    private int countEffectiveMentions(@NotNull MentionContext context) {
        int total = 0;
        for (MentionCandidate candidate : context.candidates()) {
            ResolveStatus status = candidate.resolveStatus();
            int resolvedCount = candidate.resolvedTargets().size();
            if (resolvedCount > 0 && status.countsTowardsLimit()) {
                total += resolvedCount;
            }

            int rejectedCount = 0;
            for (Map.Entry<java.util.UUID, ResolveStatus> entry : candidate.rejectedTargets().entrySet()) {
                if (entry.getValue().countsTowardsLimit()) {
                    rejectedCount++;
                }
            }
            total += rejectedCount;

            if (resolvedCount == 0 && rejectedCount == 0 && status.countsTowardsLimit()) {
                total += 1;
            }
        }
        return total;
    }
}
