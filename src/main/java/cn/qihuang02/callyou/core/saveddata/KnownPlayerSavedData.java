package cn.qihuang02.callyou.core.saveddata;

import cn.qihuang02.callyou.CallYouByYourName;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class KnownPlayerSavedData extends SavedData {
    private static final String TAG_PLAYERS = "players";
    private static final String TAG_UUID = "uuid";
    private static final String TAG_NAME = "name";

    private final Map<UUID, String> players = new HashMap<>();

    public KnownPlayerSavedData() {
    }

    public static @NotNull KnownPlayerSavedData get(@NotNull ServerLevel level) {
        ServerLevel overworld = level.getServer().overworld();
        if (overworld == null) {
            throw new IllegalStateException("Overworld level is not available for known player storage");
        }
        return overworld.getDataStorage().computeIfAbsent(
                KnownPlayerSavedData::load,
                KnownPlayerSavedData::new,
                CallYouByYourName.MODID + "_known_players"
        );
    }

    private static @NotNull KnownPlayerSavedData load(@NotNull CompoundTag tag) {
        KnownPlayerSavedData data = new KnownPlayerSavedData();
        if (!tag.contains(TAG_PLAYERS, Tag.TAG_LIST)) {
            return data;
        }
        ListTag list = tag.getList(TAG_PLAYERS, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            if (!entry.hasUUID(TAG_UUID) || !entry.contains(TAG_NAME, Tag.TAG_STRING)) {
                continue;
            }
            UUID uuid = entry.getUUID(TAG_UUID);
            String name = entry.getString(TAG_NAME);
            if (!name.isBlank()) {
                data.players.put(uuid, name);
            }
        }
        return data;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        if (players.isEmpty()) {
            return tag;
        }
        ListTag list = new ListTag();
        for (Map.Entry<UUID, String> entry : players.entrySet()) {
            String name = entry.getValue();
            if (name == null || name.isBlank()) {
                continue;
            }
            CompoundTag record = new CompoundTag();
            record.putUUID(TAG_UUID, entry.getKey());
            record.putString(TAG_NAME, name);
            list.add(record);
        }
        if (!list.isEmpty()) {
            tag.put(TAG_PLAYERS, list);
        }
        return tag;
    }

    public @NotNull Map<UUID, String> getPlayers() {
        return Map.copyOf(players);
    }

    public @Nullable String get(@NotNull UUID uuid) {
        return players.get(uuid);
    }

    public void put(@NotNull UUID uuid, @NotNull String name) {
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            return;
        }
        String previous = players.put(uuid, trimmed);
        if (!trimmed.equals(previous)) {
            setDirty();
        }
    }

    public void remove(@NotNull UUID uuid) {
        if (players.remove(uuid) != null) {
            setDirty();
        }
    }
}
