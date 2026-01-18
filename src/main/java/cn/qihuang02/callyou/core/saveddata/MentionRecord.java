package cn.qihuang02.callyou.core.saveddata;

import cn.qihuang02.callyou.api.ResolveStatus;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.Util;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record MentionRecord(
        UUID historyId,
        UUID senderId,
        String senderName,
        Component message,
        long timestamp,
        UUID targetId,
        boolean read,
        ResolveStatus status,
        String originalKey
) {
    private static final String TAG_HISTORY_ID = "history_id";
    private static final String TAG_SENDER_ID = "sender_id";
    private static final String TAG_SENDER_NAME = "sender_name";
    private static final String TAG_MESSAGE = "message";
    private static final String TAG_TIMESTAMP = "timestamp";
    private static final String TAG_TARGET_ID = "target_id";
    private static final String TAG_READ = "read";
    private static final String TAG_STATUS = "status";
    private static final String TAG_ORIGINAL_KEY = "original_key";

    private static final String TAG_TARGETS = "targets";
    private static final String TAG_READ_TARGETS = "read_targets";

    private static final Codec<ResolveStatus> STATUS_CODEC =
            Codec.STRING.xmap(ResolveStatus::valueOf, ResolveStatus::name);

    public static final Codec<MentionRecord> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.optionalFieldOf(TAG_HISTORY_ID, Util.NIL_UUID).forGetter(record -> record.historyId),
            UUIDUtil.CODEC.optionalFieldOf(TAG_SENDER_ID, Util.NIL_UUID).forGetter(record -> record.senderId),
            Codec.STRING.optionalFieldOf(TAG_SENDER_NAME, "").forGetter(record -> record.senderName),
            ComponentSerialization.CODEC.optionalFieldOf(TAG_MESSAGE, Component.empty()).forGetter(record -> record.message),
            Codec.LONG.optionalFieldOf(TAG_TIMESTAMP, 0L).forGetter(record -> record.timestamp),
            UUIDUtil.CODEC.optionalFieldOf(TAG_TARGET_ID, Util.NIL_UUID).forGetter(record -> record.targetId),
            Codec.BOOL.optionalFieldOf(TAG_READ, false).forGetter(record -> record.read),
            STATUS_CODEC.optionalFieldOf(TAG_STATUS, ResolveStatus.PENDING).forGetter(record -> record.status),
            Codec.STRING.optionalFieldOf(TAG_ORIGINAL_KEY, "").forGetter(record -> record.originalKey)
    ).apply(instance, MentionRecord::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, MentionRecord> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(CODEC);
    private static final Codec<LegacyRecord> LEGACY_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.optionalFieldOf(TAG_HISTORY_ID, Util.NIL_UUID).forGetter(record -> record.historyId),
            UUIDUtil.CODEC.optionalFieldOf(TAG_SENDER_ID, Util.NIL_UUID).forGetter(record -> record.senderId),
            Codec.STRING.optionalFieldOf(TAG_SENDER_NAME, "").forGetter(record -> record.senderName),
            ComponentSerialization.CODEC.optionalFieldOf(TAG_MESSAGE, Component.empty()).forGetter(record -> record.message),
            Codec.LONG.optionalFieldOf(TAG_TIMESTAMP, 0L).forGetter(record -> record.timestamp),
            UUIDUtil.CODEC.listOf().optionalFieldOf(TAG_TARGETS, List.of()).forGetter(record -> record.targetIds),
            UUIDUtil.CODEC.listOf().optionalFieldOf(TAG_READ_TARGETS, List.of()).forGetter(record -> record.readTargets)
    ).apply(instance, LegacyRecord::new));

    public MentionRecord(
            @NotNull UUID historyId,
            @NotNull UUID senderId,
            @NotNull String senderName,
            @NotNull Component message,
            long timestamp,
            @NotNull UUID targetId,
            boolean read,
            @NotNull ResolveStatus status,
            @NotNull String originalKey
    ) {
        this.historyId = Util.NIL_UUID.equals(historyId) ? UUID.randomUUID() : historyId;
        this.senderId = senderId;
        this.senderName = senderName == null ? "" : senderName;
        this.message = message;
        this.timestamp = timestamp;
        this.targetId = targetId;
        this.read = read;
        this.status = status;
        this.originalKey = originalKey == null ? "" : originalKey;
    }

    public static @NotNull MentionRecord create(
            @NotNull UUID senderId,
            @NotNull String senderName,
            @NotNull Component message,
            long timestamp,
            @NotNull UUID targetId,
            boolean read,
            @NotNull ResolveStatus status,
            @NotNull String originalKey
    ) {
        return new MentionRecord(UUID.randomUUID(), senderId, senderName, message, timestamp, targetId, read, status, originalKey);
    }

    public static boolean isLegacyTag(@NotNull CompoundTag tag) {
        return tag.contains(TAG_TARGETS, Tag.TAG_LIST);
    }

    public static @NotNull List<MentionRecord> fromLegacyTag(
            @NotNull CompoundTag tag,
            @NotNull RegistryOps<Tag> ops
    ) {
        Optional<LegacyRecord> legacy = LEGACY_CODEC.parse(ops, tag).result();
        if (legacy.isEmpty()) {
            return List.of();
        }
        LegacyRecord record = legacy.get();
        List<MentionRecord> result = new ArrayList<>();
        for (UUID target : record.targetIds) {
            boolean read = record.readTargets.contains(target);
            result.add(new MentionRecord(
                    record.historyId,
                    record.senderId,
                    record.senderName,
                    record.message,
                    record.timestamp,
                    target,
                    read,
                    ResolveStatus.SUCCESS,
                    ""
            ));
        }
        return List.copyOf(result);
    }

    @Override
    public @NotNull UUID historyId() {
        return historyId;
    }

    @Override
    public @NotNull UUID senderId() {
        return senderId;
    }

    @Override
    public @NotNull String senderName() {
        return senderName;
    }

    @Override
    public @NotNull Component message() {
        return message;
    }

    @Override
    public @NotNull UUID targetId() {
        return targetId;
    }

    public boolean isTarget(@NotNull UUID targetId) {
        return this.targetId.equals(targetId);
    }

    @Override
    public @NotNull ResolveStatus status() {
        return status;
    }

    @Override
    public @NotNull String originalKey() {
        return originalKey;
    }

    public @NotNull MentionRecord markRead() {
        if (read) {
            return this;
        }
        return new MentionRecord(historyId, senderId, senderName, message, timestamp, targetId, true, status, originalKey);
    }

    private record LegacyRecord(
            UUID historyId,
            UUID senderId,
            String senderName,
            Component message,
            long timestamp,
            List<UUID> targetIds,
            List<UUID> readTargets
    ) {
    }
}