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
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class MentionSavedData extends SavedData implements IPersistedSerializable {
    private static final String TAG_LOGS = "logs";

    @Persisted(key = TAG_LOGS)
    private final List<MentionRecord> allLogs = new ArrayList<>();

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
        if (data.allLogs.isEmpty() && tag.contains(TAG_LOGS, Tag.TAG_COMPOUND)) {
            data.importLegacyLogs(tag, registries);
        }
        data.normalizeRecords();
        return data;
    }

    @Contract(pure = true)
    @SkipPersistedValue(field = "allLogs")
    private boolean skipEmptyLogs(@NotNull List<MentionRecord> logs) {
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

    private void importLegacyLogs(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        LegacyLogs legacyLogs = new LegacyLogs();
        PersistedParser.deserialize(NbtOps.INSTANCE, tag, legacyLogs, registries);
        boolean changed = false;

        for (Map.Entry<UUID, List<MentionRecord>> entry : legacyLogs.playerLogs.entrySet()) {
            UUID targetId = entry.getKey();
            List<MentionRecord> records = entry.getValue();
            if (targetId == null || records == null || records.isEmpty()) {
                continue;
            }
            for (MentionRecord record : records) {
                if (record == null) {
                    continue;
                }
                MentionRecord normalized = record.normalized().withTargets(List.of(targetId));
                allLogs.add(normalized);
                changed = true;
            }
        }

        if (prune()) {
            changed = true;
        }

        if (changed) {
            setDirty();
        }
    }

    public void addLog(@NotNull MentionRecord record) {
        MentionRecord normalized = record.normalized();
        if (normalized.targetIds().isEmpty()) {
            return;
        }
        allLogs.add(normalized);
        prune();
        setDirty();
    }

    public @NotNull List<MentionRecord> getLogs(@NotNull UUID target) {
        if (allLogs.isEmpty()) {
            return List.of();
        }
        List<MentionRecord> filtered = new ArrayList<>();
        for (MentionRecord record : allLogs) {
            if (record.isTarget(target)) {
                filtered.add(record.withTargets(List.of(target)));
            }
        }
        return List.copyOf(filtered);
    }

    public int countUnread(@NotNull UUID target) {
        if (allLogs.isEmpty()) {
            return 0;
        }
        int unread = 0;
        for (MentionRecord record : allLogs) {
            if (record.isTarget(target) && !record.isRead(target)) {
                unread++;
            }
        }
        return unread;
    }

    public boolean markAsRead(@NotNull UUID target) {
        if (allLogs.isEmpty()) {
            return false;
        }
        boolean changed = false;
        for (int i = 0; i < allLogs.size(); i++) {
            MentionRecord record = allLogs.get(i);
            if (!record.isTarget(target)) {
                continue;
            }
            MentionRecord marked = record.markRead(target);
            if (marked != record) {
                allLogs.set(i, marked);
                changed = true;
            }
        }
        if (changed) {
            setDirty();
        }
        return changed;
    }

    public boolean removeRecord(@NotNull UUID target, long timestamp) {
        if (allLogs.isEmpty()) {
            return false;
        }
        boolean removed = false;
        for (int i = 0; i < allLogs.size(); i++) {
            MentionRecord record = allLogs.get(i);
            if (!record.isTarget(target) || record.timestamp() != timestamp) {
                continue;
            }
            MentionRecord updated = record.withoutTarget(target);
            if (updated == null) {
                allLogs.set(i, null);
            } else {
                allLogs.set(i, updated);
            }
            removed = true;
        }
        if (removed) {
            allLogs.removeIf(Objects::isNull);
            prune();
            setDirty();
        }
        return removed;
    }

    public void pruneOldLogs() {
        if (prune()) {
            setDirty();
        }
    }

    private boolean prune() {
        int retentionDays = Math.max(0, CallYouConfig.COMMON.historyRetentionDays.get());
        int maxHistory = Math.max(0, CallYouConfig.COMMON.maxHistoryPerPlayer.get());
        long cutoff = retentionDays > 0
                ? System.currentTimeMillis() - Duration.ofDays(retentionDays).toMillis()
                : Long.MIN_VALUE;

        boolean changed = false;

        if (retentionDays > 0 && !allLogs.isEmpty()) {
            if (allLogs.removeIf(record -> record.timestamp() < cutoff)) {
                changed = true;
            }
        }

        if (maxHistory > 0 && !allLogs.isEmpty()) {
            Map<UUID, List<Integer>> groupedIndices = new HashMap<>();
            for (int i = 0; i < allLogs.size(); i++) {
                MentionRecord record = allLogs.get(i);
                for (UUID targetId : record.targetIds()) {
                    groupedIndices.computeIfAbsent(targetId, id -> new ArrayList<>()).add(i);
                }
            }

            List<MentionRecord> updatedLogs = new ArrayList<>(allLogs);

            for (Map.Entry<UUID, List<Integer>> entry : groupedIndices.entrySet()) {
                UUID targetId = entry.getKey();
                List<Integer> indices = entry.getValue();
                if (indices.size() <= maxHistory) {
                    continue;
                }
                indices.sort(Comparator.comparingLong(idx -> updatedLogs.get(idx).timestamp()));
                int toRemove = indices.size() - maxHistory;
                for (int i = 0; i < toRemove; i++) {
                    int idx = indices.get(i);
                    MentionRecord current = updatedLogs.get(idx);
                    if (current == null) {
                        continue;
                    }
                    MentionRecord updated = current.withoutTarget(targetId);
                    if (updated == null) {
                        updatedLogs.set(idx, null);
                        changed = true;
                    } else if (updated != current) {
                        updatedLogs.set(idx, updated);
                        changed = true;
                    }
                }
            }

            if (changed) {
                updatedLogs.removeIf(Objects::isNull);
                allLogs.clear();
                allLogs.addAll(updatedLogs);
            }
        }

        return changed;
    }

    private void normalizeRecords() {
        if (allLogs.isEmpty()) {
            return;
        }
        List<MentionRecord> normalized = new ArrayList<>(allLogs.size());
        boolean changed = false;
        for (MentionRecord record : allLogs) {
            MentionRecord fixed = record.normalized();
            normalized.add(fixed);
            changed |= fixed != record;
        }
        if (changed) {
            allLogs.clear();
            allLogs.addAll(normalized);
            prune();
            setDirty();
        }
    }

    private static final class LegacyLogs implements IPersistedSerializable {
        @Persisted(key = TAG_LOGS)
        private final Map<UUID, List<MentionRecord>> playerLogs = new HashMap<>();
    }
}
