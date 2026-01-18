package cn.qihuang02.callyou.api;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;

public final class MentionCandidate {
    private final int startIndex;
    private final int endIndex;
    private final String originalText;
    private final String key;
    private final Set<UUID> resolvedTargets = new LinkedHashSet<>();
    private final Map<UUID, ResolveStatus> rejectedTargets = new LinkedHashMap<>();
    private MentionType type;
    private ResourceLocation typeId;
    private ResolveStatus resolveStatus;
    private DeliveryStatus deliveryStatus;
    private @Nullable Component detailMessage;

    public MentionCandidate(int startIndex, int endIndex, @NotNull String originalText, @NotNull String key) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.originalText = originalText;
        this.key = key;
        this.resolveStatus = ResolveStatus.PENDING;
        this.deliveryStatus = DeliveryStatus.PENDING;
    }

    public int startIndex() {
        return startIndex;
    }

    public int endIndex() {
        return endIndex;
    }

    public @NotNull String originalText() {
        return originalText;
    }

    public @NotNull String key() {
        return key;
    }

    public @NotNull String mentionToken() {
        if (key.isEmpty()) {
            return "@";
        }
        return "@" + key;
    }

    @Contract(pure = true)
    public @NotNull @UnmodifiableView Set<UUID> resolvedTargets() {
        return Collections.unmodifiableSet(resolvedTargets);
    }

    @Contract(pure = true)
    public @NotNull @UnmodifiableView Map<UUID, ResolveStatus> rejectedTargets() {
        return Collections.unmodifiableMap(rejectedTargets);
    }

    public @Nullable MentionType type() {
        return type;
    }

    public void setType(@Nullable MentionType type) {
        this.type = type;
    }

    public @Nullable ResourceLocation typeId() {
        return typeId;
    }

    public void setTypeId(@Nullable ResourceLocation typeId) {
        this.typeId = typeId;
    }

    public @NotNull ResolveStatus resolveStatus() {
        return resolveStatus;
    }

    public @NotNull DeliveryStatus deliveryStatus() {
        return deliveryStatus;
    }

    public @Nullable Component detailMessage() {
        return detailMessage;
    }

    public void addTarget(@NotNull UUID targetId) {
        resolvedTargets.add(targetId);
    }

    public void rejectTarget(@NotNull UUID targetId, @NotNull ResolveStatus status) {
        resolvedTargets.remove(targetId);
        rejectedTargets.put(targetId, status);
    }

    public void removeTarget(@NotNull UUID targetId) {
        resolvedTargets.remove(targetId);
    }

    public void updateResolveStatus(@NotNull ResolveStatus status, @Nullable Component detail) {
        this.resolveStatus = status;
        this.detailMessage = detail;
    }

    public void updateDeliveryStatus(@NotNull DeliveryStatus status, @Nullable Component detail) {
        this.deliveryStatus = status;
        this.detailMessage = detail;
    }

    public @NotNull Set<UUID> getAllAttemptedTargets() {
        Set<UUID> all = new LinkedHashSet<>(resolvedTargets);
        all.addAll(rejectedTargets.keySet());
        return all;
    }
}
