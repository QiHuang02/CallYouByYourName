package cn.qihuang02.callyou.core;

import cn.qihuang02.callyou.api.MentionContext;
import cn.qihuang02.callyou.api.MentionRules;
import cn.qihuang02.callyou.api.MentionType;
import cn.qihuang02.callyou.config.CallYouConfig;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class MentionGuard {

    private final MentionRateLimiter limiter = new MentionRateLimiter();

    public MentionGuard() {
    }

    public boolean canUseMentionType(@NotNull ServerPlayer sender,
                                     @NotNull MentionType type) {
        MentionRules rules = type.rules();

        int minOp = rules.minOpLevel();
        if (minOp > 0 && !sender.hasPermissions(minOp)) {
            return false;
        }

        if (rules.isMass()) {
            if (!CallYouPermissions.canUseMassMention(sender)) {
                return false;
            }
        } else {
            if (!CallYouPermissions.canUseMention(sender)) {
                return false;
            }
        }

        String permission = rules.permission();
        if (permission != null && !permission.isEmpty()) {
            if (!CallYouPermissions.checkLogicalPermission(sender, permission)) {
                return false;
            }
        }

        return true;
    }

    public boolean checkMessageRate(@NotNull ServerPlayer sender,
                                    int effectiveMentionCount,
                                    long nowTick) {
        CallYouConfig.Common cfg = CallYouConfig.COMMON;

        int maxMentionsPerMessage = cfg.maxMentionsPerMessage.get();
        if (maxMentionsPerMessage > 0 && effectiveMentionCount > maxMentionsPerMessage) {
            return false;
        }

        int globalCooldown = cfg.globalCooldownTicks.get();
        if (globalCooldown > 0 && !limiter.canSendMessage(sender, nowTick, globalCooldown)) {
            return false;
        }

        return true;
    }

    public @NotNull List<ServerPlayer> filterTargets(@NotNull ServerPlayer sender,
                                                     @NotNull MentionType type,
                                                     @NotNull MentionContext context,
                                                     @NotNull List<ServerPlayer> rawTargets,
                                                     long nowTick) {
        List<ServerPlayer> result = new ArrayList<>();
        if (rawTargets.isEmpty()) {
            return result;
        }

        CallYouConfig.Common cfg = CallYouConfig.COMMON;

        String key = context.mentionKey().toLowerCase(Locale.ROOT);
        int maxTargets = cfg.maxTargetsPerMention.get();
        int perTargetCooldown = cfg.perTargetCooldownTicks.get();
        UUID senderId = sender.getUUID();

        for (ServerPlayer target : rawTargets) {
            if (maxTargets > 0 && result.size() >= maxTargets) {
                break;
            }

            MentionPreferences prefs = target.getData(CallYouAttachments.MENTION_PREFERENCES.get());
            if (!prefs.isMentionAllowed(type, key, senderId)) {
                continue;
            }

            if (perTargetCooldown > 0 &&
                    !limiter.canMentionTarget(sender, target, nowTick, perTargetCooldown)) {
                continue;
            }

            result.add(target);
        }

        return result;
    }

    public void recordUsage(@NotNull ServerPlayer sender,
                            @NotNull Collection<ServerPlayer> hitTargets,
                            long nowTick) {
        limiter.record(sender, hitTargets, nowTick);
    }
}
