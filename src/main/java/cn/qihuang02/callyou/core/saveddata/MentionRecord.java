package cn.qihuang02.callyou.core.saveddata;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.syncdata.annotation.SkipPersistedValue;
import com.lowdragmc.lowdraglib2.utils.PersistedParser;
import com.mojang.serialization.Codec;
import net.minecraft.Util;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MentionRecord implements IPersistedSerializable {
    public static final Codec<MentionRecord> CODEC = PersistedParser.createCodec(MentionRecord::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, MentionRecord> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(CODEC);

    @Persisted(key = "sender_id")
    private UUID senderId = Util.NIL_UUID;

    @Persisted(key = "sender_name")
    private String senderName = "";

    @Persisted(key = "message")
    private Component message = Component.empty();

    @Persisted(key = "timestamp")
    private long timestamp;

    @Persisted(key = "location")
    private GlobalPos location;

    @Persisted(key = "targets")
    private List<UUID> targetIds = new ArrayList<>();

    @Persisted(key = "read_targets")
    private List<UUID> readTargets = new ArrayList<>();

    @Persisted(key = "target_id")
    private UUID legacyTargetId = Util.NIL_UUID;

    @Persisted(key = "read")
    private boolean legacyRead;

    public MentionRecord() {
    }

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

    @SkipPersistedValue(field = "location")
    private boolean skipMissingLocation(@Nullable GlobalPos pos) {
        return pos == null;
    }

    @SkipPersistedValue(field = "targets")
    private boolean skipEmptyTargets(@NotNull List<UUID> targets) {
        return targets.isEmpty();
    }

    @SkipPersistedValue(field = "readTargets")
    private boolean skipEmptyReadTargets(@NotNull List<UUID> targets) {
        return targets.isEmpty();
    }

    @SkipPersistedValue(field = "legacyTargetId")
    private boolean skipLegacyTarget(@NotNull UUID target) {
        return Util.NIL_UUID.equals(target);
    }

    @SkipPersistedValue(field = "legacyRead")
    private boolean skipLegacyRead(boolean read) {
        return !read;
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
