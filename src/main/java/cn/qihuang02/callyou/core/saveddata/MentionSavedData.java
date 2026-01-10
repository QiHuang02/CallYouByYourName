package cn.qihuang02.callyou.core.saveddata;

import cn.qihuang02.callyou.CallYouByYourName;
import cn.qihuang02.callyou.config.CallYouConfig;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.syncdata.annotation.SkipPersistedValue;
import com.lowdragmc.lowdraglib2.utils.PersistedParser;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.*;

public final class MentionSavedData extends SavedData implements IPersistedSerializable {
    private static final String TAG_LOGS = "logs";

    @Persisted(key = TAG_LOGS)
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
        PersistedParser.deserialize(NbtOps.INSTANCE, tag, data, registries);
        return data;
    }

    @Contract(pure = true)
    @SkipPersistedValue(field = "playerLogs")
    private boolean skipEmptyLogs(@NotNull Map<UUID, List<MentionRecord>> logs) {
        return logs.isEmpty();
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        PersistedParser.serialize(NbtOps.INSTANCE, this, registries)
                .resultOrPartial(CallYouByYourName.LOGGER::error)
                .ifPresent(encoded -> {
                    if (encoded instanceof CompoundTag compound) {
                        tag.merge(compound);
                    }
                });
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
}
