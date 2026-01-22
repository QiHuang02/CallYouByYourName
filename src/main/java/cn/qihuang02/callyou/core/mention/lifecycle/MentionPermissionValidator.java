package cn.qihuang02.callyou.core.mention.lifecycle;

import cn.qihuang02.callyou.api.*;
import cn.qihuang02.callyou.core.attachment.CallYouAttachments;
import cn.qihuang02.callyou.core.attachment.MentionPreferences;
import cn.qihuang02.callyou.core.mention.MentionGuard;
import cn.qihuang02.callyou.core.mention.components.formatter.ItemTextFormatter;
import cn.qihuang02.callyou.core.saveddata.MentionPreferencesSavedData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class MentionPermissionValidator implements MentionLifeCycle {
    private final MentionGuard guard;

    public MentionPermissionValidator(@NotNull MentionGuard guard) {
        this.guard = guard;
    }

    @Override
    public void process(@NotNull MentionContext context) {
        ServerPlayer sender = context.sender();
        MinecraftServer server = context.server();
        MentionPreferencesSavedData preferencesData = MentionPreferencesSavedData.get(context.level());
        boolean itemMentionUsed = false;

        for (MentionCandidate candidate : context.candidates()) {
            MentionType type = candidate.type();
            if (type == null) {
                continue;
            }
            if (candidate.resolveStatus().isError()) {
                continue;
            }

            if (isItemMention(type)) {
                if (itemMentionUsed) {
                    candidate.updateResolveStatus(ResolveStatus.OFFLINE_IGNORED, null);
                    candidate.updateDeliveryStatus(DeliveryStatus.SKIPPED, null);
                    continue;
                }
                itemMentionUsed = true;

                if (sender.getMainHandItem().isEmpty()) {
                    Component error = Component.translatable("message.callyou.item.empty");
                    candidate.updateResolveStatus(ResolveStatus.NOT_FOUND, error);
                    continue;
                }
            }

            if (!guard.canUseMentionType(sender, type, candidate.typeId())) {
                Component display = Component.literal(candidate.mentionToken());
                Component error = Component.translatable("message.callyou.no_permission", display);
                rejectAll(candidate, ResolveStatus.NO_PERMISSION);
                candidate.updateResolveStatus(ResolveStatus.NO_PERMISSION, error);
                continue;
            }

            if (server != null && !candidate.resolvedTargets().isEmpty()) {
                List<UUID> targets = new ArrayList<>(candidate.resolvedTargets());
                for (UUID targetId : targets) {
                    ServerPlayer target = server.getPlayerList().getPlayer(targetId);
                    MentionPreferences prefs = target != null
                            ? target.getData(CallYouAttachments.MENTION_PREFERENCES.get())
                            : preferencesData.getPreferences(targetId);
                    if (!prefs.isMentionAllowed(type, candidate.typeId(), context.senderId())) {
                        candidate.rejectTarget(targetId, ResolveStatus.BLOCKED);
                    }
                }
            }

            if (candidate.resolvedTargets().isEmpty()) {
                if (candidate.resolveStatus() == ResolveStatus.PENDING) {
                    boolean allBlocked = !candidate.rejectedTargets().isEmpty()
                            && candidate.rejectedTargets().values().stream().allMatch(status -> status == ResolveStatus.BLOCKED);
                    candidate.updateResolveStatus(allBlocked ? ResolveStatus.BLOCKED : ResolveStatus.EMPTY_GROUP, null);
                }
                continue;
            }

            boolean hasOnline = false;
            boolean hasOffline = false;
            if (server != null) {
                for (UUID targetId : candidate.resolvedTargets()) {
                    if (server.getPlayerList().getPlayer(targetId) != null) {
                        hasOnline = true;
                    } else {
                        hasOffline = true;
                    }
                }
            }

            if (hasOnline) {
                candidate.updateResolveStatus(ResolveStatus.SUCCESS, null);
            } else if (hasOffline) {
                candidate.updateResolveStatus(ResolveStatus.SUCCESS_OFFLINE, null);
            } else if (candidate.resolveStatus() == ResolveStatus.PENDING) {
                candidate.updateResolveStatus(ResolveStatus.SUCCESS, null);
            }
        }
    }

    private boolean isItemMention(@NotNull MentionType type) {
        return type.textFormatter() instanceof ItemTextFormatter;
    }

    private void rejectAll(@NotNull MentionCandidate candidate, @NotNull ResolveStatus status) {
        List<UUID> targets = new ArrayList<>(candidate.resolvedTargets());
        for (UUID targetId : targets) {
            candidate.rejectTarget(targetId, status);
        }
    }
}
