package cn.qihuang02.callyou.core.saveddata;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.Util;
import net.minecraft.core.GlobalPos;
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
    private static final String TAG_SENDER_ID = "sender_id";
    private static final String TAG_SENDER_NAME = "sender_name";
    private static final String TAG_MESSAGE = "message";
    private static final String TAG_TIMESTAMP = "timestamp";
    private static final String TAG_LOCATION = "location";
    private static final String TAG_TARGETS = "targets";
    private static final String TAG_READ_TARGETS = "read_targets";
    private static final String TAG_LEGACY_TARGET_ID = "target_id";
    private static final String TAG_LEGACY_READ = "read";

    public static final Codec<MentionRecord> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.optionalFieldOf(TAG_SENDER_ID, Util.NIL_UUID).forGetter(record -> record.senderId),
            Codec.STRING.optionalFieldOf(TAG_SENDER_NAME, "").forGetter(record -> record.senderName),
            ComponentSerialization.CODEC.optionalFieldOf(TAG_MESSAGE, Component.empty()).forGetter(record -> record.message),
            Codec.LONG.optionalFieldOf(TAG_TIMESTAMP, 0L).forGetter(record -> record.timestamp),
            GlobalPos.CODEC.optionalFieldOf(TAG_LOCATION).forGetter(record -> Optional.ofNullable(record.location)),
            UUIDUtil.CODEC.listOf().optionalFieldOf(TAG_TARGETS).forGetter(record -> record.targetIds.isEmpty()
                    ? Optional.empty()
                    : Optional.of(record.targetIds)),
            UUIDUtil.CODEC.listOf().optionalFieldOf(TAG_READ_TARGETS).forGetter(record -> record.readTargets.isEmpty()
                    ? Optional.empty()
                    : Optional.of(record.readTargets)),
            UUIDUtil.CODEC.optionalFieldOf(TAG_LEGACY_TARGET_ID).forGetter(record -> Util.NIL_UUID.equals(record.legacyTargetId)
                    ? Optional.empty()
                    : Optional.of(record.legacyTargetId)),
            Codec.BOOL.optionalFieldOf(TAG_LEGACY_READ).forGetter(record -> record.legacyRead
                    ? Optional.of(true)
                    : Optional.empty())
    ).apply(instance, MentionRecord::fromCodec));
    public static final StreamCodec<RegistryFriendlyByteBuf, MentionRecord> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(CODEC);

    private UUID senderId = Util.NIL_UUID;

    private String senderName = "";

    private Component message = Component.empty();

    private long timestamp;

    private GlobalPos location;

    private List<UUID> targetIds = new ArrayList<>();

    private List<UUID> readTargets = new ArrayList<>();

    private UUID legacyTargetId = Util.NIL_UUID;

    private boolean legacyRead;

    public MentionRecord(
            @NotNull UUID senderId,
            @NotNull String senderName,
            @NotNull Component message,
            long timestamp,
            @NotNull Collection<UUID> targets,
            @NotNull Collection<UUID> readTargets,
            @Nullable GlobalPos location
    ) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.message = message;
        this.timestamp = timestamp;
        this.location = location;
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
            @NotNull Collection<UUID> targets,
            @Nullable GlobalPos location
    ) {
        return new MentionRecord(senderId, senderName, message, timestamp, targets, List.of(), location);
    }

    private static @NotNull MentionRecord fromCodec(
            @NotNull UUID senderId,
            @NotNull String senderName,
            @NotNull Component message,
            long timestamp,
            @NotNull Optional<GlobalPos> location,
            @NotNull Optional<List<UUID>> targets,
            @NotNull Optional<List<UUID>> readTargets,
            @NotNull Optional<UUID> legacyTargetId,
            @NotNull Optional<Boolean> legacyRead
    ) {
        MentionRecord record = new MentionRecord(
                senderId,
                senderName,
                message,
                timestamp,
                targets.orElse(List.of()),
                readTargets.orElse(List.of()),
                location.orElse(null)
        );
        record.legacyTargetId = legacyTargetId.orElse(Util.NIL_UUID);
        record.legacyRead = legacyRead.orElse(false);
        return record;
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

    public @Nullable GlobalPos location() {
        return location;
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
        return new MentionRecord(senderId, senderName, message, timestamp, targetIds, updatedRead, location);
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
        return new MentionRecord(senderId, senderName, message, timestamp, sanitized, filteredRead, location);
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
        return new MentionRecord(senderId, senderName, message, timestamp, remainingTargets, remainingRead, location);
    }

    public @NotNull MentionRecord normalized() {
        List<UUID> normalizedTargets = targetIds;
        List<UUID> normalizedRead = readTargets;

        if (normalizedTargets.isEmpty() && !Util.NIL_UUID.equals(legacyTargetId)) {
            normalizedTargets = List.of(legacyTargetId);
        }
        if (legacyRead && !Util.NIL_UUID.equals(legacyTargetId) && !normalizedRead.contains(legacyTargetId)) {
            List<UUID> updatedRead = new ArrayList<>(normalizedRead);
            updatedRead.add(legacyTargetId);
            normalizedRead = updatedRead;
        }
        return new MentionRecord(senderId, senderName, message, timestamp, normalizedTargets, normalizedRead, location);
    }
}
