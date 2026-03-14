package cn.qihuang02.callyou.api;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class MentionContext {
    private final ServerPlayer sender;
    private final Component originalMessage;
    private final String rawText;
    private final List<MentionCandidate> candidates = new ArrayList<>();
    private @Nullable Component finalMessage;
    private @Nullable Component senderView;
    private List<ServerPlayer> notifiedTargets = List.of();

    public MentionContext(
            @NotNull ServerPlayer sender,
            @NotNull Component originalMessage,
            @NotNull String rawText
    ) {
        this.sender = sender;
        this.originalMessage = originalMessage;
        this.rawText = rawText;
    }

    public @NotNull ServerPlayer sender() {
        return sender;
    }

    public @NotNull Component originalMessage() {
        return originalMessage;
    }

    public @NotNull String rawText() {
        return rawText;
    }

    public void addCandidate(@NotNull MentionCandidate candidate) {
        candidates.add(candidate);
    }

    @Contract(pure = true)
    public @NotNull @Unmodifiable List<MentionCandidate> candidates() {
        return List.copyOf(candidates);
    }

    public @NotNull List<MentionCandidate> getSuccessfulCandidates() {
        if (candidates.isEmpty()) {
            return List.of();
        }
        List<MentionCandidate> result = new ArrayList<>();
        for (MentionCandidate candidate : candidates) {
            if (!candidate.resolveStatus().isSuccess()) {
                continue;
            }
            DeliveryStatus deliveryStatus = candidate.deliveryStatus();
            if (deliveryStatus == DeliveryStatus.RATE_LIMITED || deliveryStatus == DeliveryStatus.SKIPPED) {
                continue;
            }
            result.add(candidate);
        }
        return List.copyOf(result);
    }

    public @Nullable Component finalMessage() {
        return finalMessage;
    }

    public void setFinalMessage(@Nullable Component finalMessage) {
        this.finalMessage = finalMessage;
    }

    public @Nullable Component senderView() {
        return senderView;
    }

    public void setSenderView(@Nullable Component senderView) {
        this.senderView = senderView;
    }

    @Contract(pure = true)
    public @NotNull @Unmodifiable List<ServerPlayer> notifiedTargets() {
        return List.copyOf(notifiedTargets);
    }

    public void setNotifiedTargets(@NotNull List<ServerPlayer> notifiedTargets) {
        this.notifiedTargets = List.copyOf(notifiedTargets);
    }

    public @Nullable MinecraftServer server() {
        return sender.getServer();
    }

    public @NotNull ServerLevel level() {
        return sender.serverLevel();
    }

    public @NotNull ResourceKey<Level> dimension() {
        return sender.level().dimension();
    }

    public @NotNull UUID senderId() {
        return sender.getUUID();
    }

    public String senderName() {
        return sender.getGameProfile().getName();
    }

    public @NotNull Component senderDisplayName() {
        return sender.getDisplayName();
    }
}
