package cn.qihuang02.callyou.core.saveddata;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.Util;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MentionRecord {
    private static final String TAG_HISTORY_ID = "history_id";
    private static final String TAG_SENDER_ID = "sender_id";
    private static final String TAG_SENDER_NAME = "sender_name";
    private static final String TAG_MESSAGE = "message";
    private static final String TAG_TIMESTAMP = "timestamp";
    private static final String TAG_TARGETS = "targets";
    private static final String TAG_READ_TARGETS = "read_targets";

    public static final Codec<MentionRecord> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.optionalFieldOf(TAG_HISTORY_ID, Util.NIL_UUID).forGetter(record -> record.historyId),
            UUIDUtil.CODEC.optionalFieldOf(TAG_SENDER_ID, Util.NIL_UUID).forGetter(record -> record.senderId),
            Codec.STRING.optionalFieldOf(TAG_SENDER_NAME, "").forGetter(record -> record.senderName),
            ComponentSerialization.CODEC.optionalFieldOf(TAG_MESSAGE, Component.empty()).forGetter(record -> record.message),
            Codec.LONG.optionalFieldOf(TAG_TIMESTAMP, 0L).forGetter(record -> record.timestamp),
            UUIDUtil.CODEC.listOf().optionalFieldOf(TAG_TARGETS, List.of()).forGetter(record -> record.targetIds),
            UUIDUtil.CODEC.listOf().optionalFieldOf(TAG_READ_TARGETS, List.of()).forGetter(record -> record.readTargets)
    ).apply(instance, MentionRecord::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, MentionRecord> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(CODEC);

    private final UUID historyId;

    private final UUID senderId;

    private final String senderName;

    private final Component message;

    private final long timestamp;

    private List<UUID> targetIds = new ArrayList<>();

    private List<UUID> readTargets = new ArrayList<>();

    public MentionRecord(
            @NotNull UUID historyId,
            @NotNull UUID senderId,
            @NotNull String senderName,
            @NotNull Component message,
            long timestamp,
            @NotNull Collection<UUID> targets,
            @NotNull Collection<UUID> readTargets
    ) {
        this.historyId = Util.NIL_UUID.equals(historyId) ? UUID.randomUUID() : historyId;
        this.senderId = senderId;
        this.senderName = senderName == null ? "" : senderName;
        this.message = message;
        this.timestamp = timestamp;
        this.targetIds = sanitizeTargets(targets);
        List<UUID> sanitizedReads = sanitizeTargets(readTargets);
        sanitizedReads.retainAll(this.targetIds);
        this.readTargets = sanitizedReads;
    }

    public static @NotNull MentionRecord create(
            @NotNull UUID senderId,
            @NotNull String senderName,
            @NotNull Component message,
            long timestamp,
            @NotNull Collection<UUID> targets
    ) {
        return create(senderId, senderName, message, timestamp, targets, List.of());
    }

    public static @NotNull MentionRecord create(
            @NotNull UUID senderId,
            @NotNull String senderName,
            @NotNull Component message,
            long timestamp,
            @NotNull Collection<UUID> targets,
            @NotNull Collection<UUID> readTargets
    ) {
        return new MentionRecord(UUID.randomUUID(), senderId, senderName, message, timestamp, targets, readTargets);
    }

    public @NotNull UUID historyId() {
        return historyId;
    }

    private @NotNull List<UUID> sanitizeTargets(@NotNull Collection<UUID> targets) {
        Set<UUID> unique = new LinkedHashSet<>();
        for (UUID id : targets) {
            if (id != null && !Util.NIL_UUID.equals(id)) {
                unique.add(id);
            }
        }
        return new ArrayList<>(unique);
    }

    public @NotNull UUID senderId() {
        return senderId;
    }

    public @NotNull String senderName() {
        return senderName;
    }

    public @NotNull Component message() {
        return message;
    }

    public long timestamp() {
        return timestamp;
    }

    public @NotNull List<UUID> targetIds() {
        return List.copyOf(targetIds);
    }

    public boolean isTarget(@NotNull UUID targetId) {
        return targetIds.contains(targetId);
    }

    public boolean isRead(@NotNull UUID targetId) {
        return readTargets.contains(targetId);
    }

    public @NotNull MentionRecord markRead(@NotNull UUID targetId) {
        if (!isTarget(targetId) || isRead(targetId)) {
            return this;
        }
        List<UUID> updatedRead = new ArrayList<>(readTargets);
        updatedRead.add(targetId);
        return new MentionRecord(historyId, senderId, senderName, message, timestamp, targetIds, updatedRead);
    }

    public @NotNull MentionRecord withTargets(@NotNull Collection<UUID> targets) {
        List<UUID> sanitized = sanitizeTargets(targets);
        if (sanitized.equals(this.targetIds)) {
            return this;
        }
        List<UUID> filteredRead = new ArrayList<>();
        for (UUID id : readTargets) {
            if (sanitized.contains(id)) {
                filteredRead.add(id);
            }
        }
        return new MentionRecord(historyId, senderId, senderName, message, timestamp, sanitized, filteredRead);
    }

    public @Nullable MentionRecord withoutTarget(@NotNull UUID targetId) {
        if (!isTarget(targetId)) {
            return this;
        }
        List<UUID> remainingTargets = new ArrayList<>(targetIds);
        remainingTargets.remove(targetId);
        if (remainingTargets.isEmpty()) {
            return null;
        }
        List<UUID> remainingRead = new ArrayList<>(readTargets);
        remainingRead.remove(targetId);
        return new MentionRecord(historyId, senderId, senderName, message, timestamp, remainingTargets, remainingRead);
    }
}
