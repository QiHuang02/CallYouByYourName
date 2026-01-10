package cn.qihuang02.callyou.core;

import cn.qihuang02.callyou.api.MentionContext;
import cn.qihuang02.callyou.api.MentionType;
import cn.qihuang02.callyou.api.components.MentionRules;
import cn.qihuang02.callyou.config.CallYouConfig;
import cn.qihuang02.callyou.core.attachment.CallYouAttachments;
import cn.qihuang02.callyou.core.attachment.MentionPreferences;
import cn.qihuang02.callyou.core.handler.PermissionsHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public final class MentionGuard {
    private final MentionRateLimiter limiter = new MentionRateLimiter();

    public boolean canUseMentionType(
            @NotNull ServerPlayer sender,
            @NotNull MentionType type,
            @Nullable ResourceLocation mentionID
    ) {
        MentionRules rules = type.rules();

        int minOp = rules.minOpLevel();
        if (minOp > 0 && !sender.hasPermissions(minOp)) {
            return false;
        }

        if (rules.isMass()) {
            if (!PermissionsHandler.canUseMassMention(sender)) {
                return false;
            }
        } else {
            if (!PermissionsHandler.canUseMention(sender)) {
                return false;
            }
        }

        return mentionID == null || PermissionsHandler.canUseMentionType(sender, mentionID);
    }

    public boolean checkMessageRate(
            @NotNull ServerPlayer sender,
            int effectiveMentionCount,
            long nowTick
    ) {
        CallYouConfig.Common cfg = CallYouConfig.COMMON;

        int maxMentionsPerMessage = cfg.maxMentionsPerMessage.get();
        if (maxMentionsPerMessage > 0 && effectiveMentionCount > maxMentionsPerMessage) {
            return false;
        }

        int globalCooldown = cfg.globalCooldownTicks.get();
        return globalCooldown <= 0 || limiter.canSendMessage(sender, nowTick, globalCooldown);
    }

    public @NotNull List<ServerPlayer> filterTargets(
            @NotNull ServerPlayer sender,
            @NotNull MentionType type,
            @Nullable ResourceLocation mentionID,
            @NotNull MentionContext context,
            @NotNull List<ServerPlayer> rawTargets,
            long nowTick
    ) {
        List<ServerPlayer> result = new ArrayList<>();
        if (rawTargets.isEmpty()) {
            return result;
        }

        CallYouConfig.Common cfg = CallYouConfig.COMMON;

        int maxTargets = cfg.maxTargetsPerMention.get();
        int perTargetCooldown = cfg.perTargetCooldownTicks.get();
        UUID senderId = context.senderId();

        for (ServerPlayer target : rawTargets) {
            if (maxTargets > 0 && result.size() >= maxTargets) {
                break;
            }

            MentionPreferences prefs = target.getData(CallYouAttachments.MENTION_PREFERENCES.get());
            if (!prefs.isMentionAllowed(type, mentionID, senderId)) {
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

    public void recordUsage(
            @NotNull ServerPlayer sender,
            @NotNull Collection<ServerPlayer> hitTargets,
            long nowTick
    ) {
        limiter.record(sender, hitTargets, nowTick);
    }

    public void onPlayerLogout(@NotNull ServerPlayer player) {
        limiter.onPlayerLogout(player.getUUID());
    }
}
