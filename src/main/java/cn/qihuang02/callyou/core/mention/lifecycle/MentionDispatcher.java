package cn.qihuang02.callyou.core.mention.lifecycle;

import cn.qihuang02.callyou.api.*;
import cn.qihuang02.callyou.api.components.Notifier;
import cn.qihuang02.callyou.api.event.MentionEvent;
import cn.qihuang02.callyou.core.attachment.CallYouAttachments;
import cn.qihuang02.callyou.core.attachment.MentionPreferences;
import cn.qihuang02.callyou.core.mention.MentionGuard;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class MentionDispatcher implements MentionLifeCycle {
    private final MentionGuard guard;

    public MentionDispatcher(@NotNull MentionGuard guard) {
        this.guard = guard;
    }

    @Override
    public void process(@NotNull MentionContext context) {
        MinecraftServer server = context.server();
        if (server == null) {
            return;
        }

        long nowTick = server.getTickCount();
        Set<ServerPlayer> allTargetsHit = new java.util.LinkedHashSet<>();

        for (MentionCandidate candidate : context.candidates()) {
            if (candidate.type() == null) {
                continue;
            }
            if (candidate.deliveryStatus() == DeliveryStatus.RATE_LIMITED) {
                firePreEvent(context, candidate);
                continue;
            }
            if (!candidate.resolveStatus().isSuccess()) {
                continue;
            }
            allTargetsHit.addAll(dispatchSingle(context, candidate, nowTick));
        }

        context.setNotifiedTargets(new ArrayList<>(allTargetsHit));
    }

    private @NotNull List<ServerPlayer> dispatchSingle(
            @NotNull MentionContext context,
            @NotNull MentionCandidate candidate,
            long nowTick
    ) {
        MentionType type = candidate.type();
        if (type == null) {
            return List.of();
        }

        if (!firePreEvent(context, candidate)) {
            candidate.updateDeliveryStatus(DeliveryStatus.SKIPPED, null);
            return List.of();
        }

        if (candidate.resolveStatus().isError()) {
            return List.of();
        }

        MinecraftServer server = context.server();
        if (server == null || candidate.resolvedTargets().isEmpty()) {
            return List.of();
        }

        List<ServerPlayer> resolvedOnline = resolveOnline(server, candidate.resolvedTargets());
        if (resolvedOnline.isEmpty()) {
            return List.of();
        }

        List<ServerPlayer> filteredTargets =
                guard.filterTargets(context.sender(), type, candidate.typeId(), context, resolvedOnline, nowTick);

        if (filteredTargets.isEmpty()) {
            markRejectedOnline(candidate, resolvedOnline, Set.of());
            if (candidate.resolvedTargets().isEmpty() && candidate.resolveStatus().isSuccess()) {
                candidate.updateResolveStatus(ResolveStatus.OFFLINE_IGNORED, null);
            }
            return List.of();
        }

        Set<UUID> allowedAfterGuard = filteredTargets.stream()
                .map(ServerPlayer::getUUID)
                .collect(Collectors.toSet());
        markRejectedOnline(candidate, resolvedOnline, allowedAfterGuard);

        List<ServerPlayer> finalTargets = new ArrayList<>();
        for (ServerPlayer target : filteredTargets) {
            MentionPreferences prefs = CallYouAttachments.getPreferences(target);
            ResourceLocation typeId = candidate.typeId();
            if (typeId != null && !prefs.isNotifierEnabled(typeId)) {
                continue;
            }
            finalTargets.add(target);
        }

        if (finalTargets.isEmpty()) {
            return List.of();
        }

        Notifier notifier = type.notifier();
        notifier.apply(context, candidate, finalTargets);
        candidate.updateDeliveryStatus(DeliveryStatus.SENT, null);

        MinecraftForge.EVENT_BUS.post(new MentionEvent.Post(context, candidate));

        return finalTargets;
    }

    private boolean firePreEvent(@NotNull MentionContext context, @NotNull MentionCandidate candidate) {
        MentionEvent.Pre preEvent = new MentionEvent.Pre(context, candidate);
        return !MinecraftForge.EVENT_BUS.post(preEvent);
    }

    private @NotNull List<ServerPlayer> resolveOnline(
            @NotNull MinecraftServer server,
            @NotNull Set<UUID> targetIds
    ) {
        List<ServerPlayer> resolved = new ArrayList<>();
        for (UUID id : targetIds) {
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (player != null) {
                resolved.add(player);
            }
        }
        return resolved;
    }

    private void markRejectedOnline(
            @NotNull MentionCandidate candidate,
            @NotNull List<ServerPlayer> resolvedOnline,
            @NotNull Set<UUID> allowedOnline
    ) {
        for (ServerPlayer player : resolvedOnline) {
            UUID id = player.getUUID();
            if (!allowedOnline.contains(id)) {
                candidate.rejectTarget(id, ResolveStatus.OFFLINE_IGNORED);
            }
        }
    }
}
