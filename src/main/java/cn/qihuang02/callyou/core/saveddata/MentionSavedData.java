package cn.qihuang02.callyou.core.saveddata;

import cn.qihuang02.callyou.CallYouByYourName;
import cn.qihuang02.callyou.config.CallYouConfig;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.*;

public final class MentionSavedData extends SavedData {
    private static final String TAG_LOGS = "logs";

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
        if (tag.contains(TAG_LOGS, Tag.TAG_LIST)) {
            ListTag list = tag.getList(TAG_LOGS, Tag.TAG_COMPOUND);
            RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, registries);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag recordTag = list.getCompound(i);
                MentionRecord.CODEC.parse(ops, recordTag)
                        .resultOrPartial(CallYouByYourName.LOGGER::error)
                        .ifPresent(data.allLogs::add);
            }
        } else if (tag.contains(TAG_LOGS, Tag.TAG_COMPOUND)) {
            data.importLegacyLogs(tag, registries);
        }
        return data;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        if (allLogs.isEmpty()) {
            return tag;
        }
        RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, registries);
        ListTag list = new ListTag();
        for (MentionRecord record : allLogs) {
            MentionRecord.CODEC.encodeStart(ops, record)
                    .resultOrPartial(CallYouByYourName.LOGGER::error)
                    .ifPresent(encoded -> {
                        if (encoded instanceof CompoundTag compound) {
                            list.add(compound);
                        }
                    });
        }
        if (!list.isEmpty()) {
            tag.put(TAG_LOGS, list);
        }
        return tag;
    }

    private void importLegacyLogs(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        boolean changed = false;
        int originalSize = allLogs.size();
        CompoundTag legacyLogs = tag.getCompound(TAG_LOGS);
        if (!legacyLogs.isEmpty()) {
            RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, registries);
            for (String key : legacyLogs.getAllKeys()) {
                UUID targetId;
                try {
                    targetId = UUID.fromString(key);
                } catch (IllegalArgumentException ex) {
                    continue;
                }
                ListTag records = legacyLogs.getList(key, Tag.TAG_COMPOUND);
                if (records.isEmpty()) {
                    continue;
                }
                for (int i = 0; i < records.size(); i++) {
                    CompoundTag recordTag = records.getCompound(i);
                    MentionRecord.CODEC.parse(ops, recordTag)
                            .resultOrPartial(CallYouByYourName.LOGGER::error)
                            .ifPresent(record -> allLogs.add(record.withTargets(List.of(targetId))));
                }
            }
        }
        if (allLogs.size() != originalSize) {
            changed = true;
        }

        if (prune()) {
            changed = true;
        }

        if (changed) {
            setDirty();
        }
    }

    public void addLog(@NotNull MentionRecord record) {
        if (record.targetIds().isEmpty()) {
            return;
        }
        allLogs.add(record);
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

    public boolean markAsRead(@NotNull UUID target, @NotNull UUID historyId) {
        if (allLogs.isEmpty()) {
            return false;
        }
        boolean changed = false;
        for (int i = 0; i < allLogs.size(); i++) {
            MentionRecord record = allLogs.get(i);
            if (!record.isTarget(target) || !record.historyId().equals(historyId)) {
                continue;
            }
            MentionRecord marked = record.markRead(target);
            if (marked != record) {
                allLogs.set(i, marked);
                changed = true;
            }
            break;
        }
        if (changed) {
            setDirty();
        }
        return changed;
    }

    public boolean removeRecord(@NotNull UUID target, @NotNull UUID historyId) {
        if (allLogs.isEmpty()) {
            return false;
        }
        boolean removed = false;
        for (int i = 0; i < allLogs.size(); i++) {
            MentionRecord record = allLogs.get(i);
            if (!record.isTarget(target) || !record.historyId().equals(historyId)) {
                continue;
            }
            MentionRecord updated = record.withoutTarget(target);
            if (updated == null) {
                allLogs.set(i, null);
            } else {
                allLogs.set(i, updated);
            }
            removed = true;
            break;
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


}
