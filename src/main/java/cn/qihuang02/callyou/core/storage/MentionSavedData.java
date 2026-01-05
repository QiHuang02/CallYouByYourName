package cn.qihuang02.callyou.core.storage;

import cn.qihuang02.callyou.CallYouByYourName;
import cn.qihuang02.callyou.config.CallYouConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.*;

public final class MentionSavedData extends SavedData {
    private static final String TAG_LOGS = "logs";
    private static final Codec<Map<UUID, List<MentionRecord>>> LOGS_CODEC = Codec.unboundedMap(
            Codec.STRING.xmap(UUID::fromString, UUID::toString),
            MentionRecord.CODEC.listOf()
    );

    private final Map<UUID, List<MentionRecord>> playerLogs = new HashMap<>();

    public static @NotNull MentionSavedData get(@NotNull ServerLevel level) {
        ServerLevel overworld = level.getServer().overworld();
        if (overworld == null) {
            throw new IllegalStateException("Overworld level is not available for mention storage");
        }
        return overworld.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(
                        MentionSavedData::new,
                        MentionSavedData::load,
                        null
                ),
                CallYouByYourName.MODID + "_mention_logs"
        );
    }

    private static @NotNull MentionSavedData load(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        MentionSavedData data = new MentionSavedData();
        if (tag.contains(TAG_LOGS)) {
            DataResult<Map<UUID, List<MentionRecord>>> decoded = decode(TAG_LOGS, tag, LOGS_CODEC, NbtOps.INSTANCE);
            decoded.result().ifPresent(map -> map.forEach((uuid, logs) ->
                    data.playerLogs.put(uuid, new ArrayList<>(logs))));
        }
        return data;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        encodeAndPut(TAG_LOGS, tag, LOGS_CODEC, playerLogs, NbtOps.INSTANCE);
        return tag;
    }

    public void addLog(@NotNull UUID target, @NotNull MentionRecord record) {
        List<MentionRecord> list = playerLogs.computeIfAbsent(target, id -> new ArrayList<>());
        list.add(record);
        prune(target, list);
        setDirty();
    }

    public @NotNull List<MentionRecord> getLogs(@NotNull UUID target) {
        List<MentionRecord> list = playerLogs.get(target);
        if (list == null) {
            return List.of();
        }
        return List.copyOf(list);
    }

    public int countUnread(@NotNull UUID target) {
        List<MentionRecord> list = playerLogs.get(target);
        if (list == null) {
            return 0;
        }
        int unread = 0;
        for (MentionRecord record : list) {
            if (!record.read()) {
                unread++;
            }
        }
        return unread;
    }

    public boolean markAsRead(@NotNull UUID target) {
        List<MentionRecord> list = playerLogs.get(target);
        if (list == null || list.isEmpty()) {
            return false;
        }
        boolean changed = false;
        List<MentionRecord> updated = new ArrayList<>(list.size());
        for (MentionRecord record : list) {
            MentionRecord marked = record.markRead();
            updated.add(marked);
            changed |= marked != record;
        }
        if (changed) {
            playerLogs.put(target, updated);
            setDirty();
        }
        return changed;
    }

    public boolean removeRecord(@NotNull UUID target, long timestamp) {
        List<MentionRecord> list = playerLogs.get(target);
        if (list == null || list.isEmpty()) {
            return false;
        }
        boolean removed = list.removeIf(record -> record.timestamp() == timestamp);
        if (removed) {
            prune(target, list);
            setDirty();
        }
        return removed;
    }

    public void pruneOldLogs() {
        boolean changed = false;
        Iterator<Map.Entry<UUID, List<MentionRecord>>> iterator = playerLogs.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, List<MentionRecord>> entry = iterator.next();
            List<MentionRecord> logs = entry.getValue();
            if (logs == null || logs.isEmpty()) {
                iterator.remove();
                changed = true;
                continue;
            }
            changed |= prune(entry.getKey(), logs);
            if (logs.isEmpty()) {
                iterator.remove();
                changed = true;
            }
        }
        if (changed) {
            setDirty();
        }
    }

    private boolean prune(@NotNull UUID uuid, @NotNull List<MentionRecord> logs) {
        int retentionDays = Math.max(0, CallYouConfig.COMMON.historyRetentionDays.get());
        int maxHistory = Math.max(0, CallYouConfig.COMMON.maxHistoryPerPlayer.get());
        long cutoff = retentionDays > 0
                ? System.currentTimeMillis() - Duration.ofDays(retentionDays).toMillis()
                : Long.MIN_VALUE;

        boolean changed = retentionDays > 0 && logs.removeIf(record -> record.timestamp() < cutoff);

        if (maxHistory > 0 && logs.size() > maxHistory) {
            logs.sort(Comparator.comparingLong(MentionRecord::timestamp));
            int toRemove = logs.size() - maxHistory;
            if (toRemove > 0) {
                logs.subList(0, toRemove).clear();
                changed = true;
            }
        }

        if (logs.isEmpty()) {
            playerLogs.remove(uuid);
            changed = true;
        }
        return changed;
    }

    private static <T> void encodeAndPut(
            @NotNull String key,
            @NotNull CompoundTag target,
            @NotNull Codec<T> codec,
            @NotNull T value,
            @NotNull DynamicOps<Tag> ops
    ) {
        DataResult<Tag> encoded = codec.encodeStart(ops, value);
        encoded.result().ifPresent(tag -> target.put(key, tag));
    }

    private static <T> DataResult<T> decode(
            @NotNull String key,
            @NotNull CompoundTag source,
            @NotNull Codec<T> codec,
            @NotNull DynamicOps<Tag> ops
    ) {
        Tag element = source.get(key);
        if (element == null) {
            return DataResult.error(() -> "Missing tag: " + key);
        }
        return codec.parse(ops, element);
    }
}
