package cn.qihuang02.callyou.core.saveddata;

import cn.qihuang02.callyou.CallYouByYourName;
import cn.qihuang02.callyou.core.attachment.MentionPreferences;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class MentionPreferencesSavedData extends SavedData {
    private static final String TAG_PREFS = "prefs";
    private static final String TAG_PLAYER = "player";
    private static final String TAG_DATA = "data";

    private final Map<UUID, MentionPreferences> preferencesByPlayer = new HashMap<>();

    public static @NotNull MentionPreferencesSavedData get(@NotNull ServerLevel level) {
        ServerLevel overworld = level.getServer().overworld();
        if (overworld == null) {
            throw new IllegalStateException("Overworld level is not available for mention preferences");
        }
        return overworld.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(
                        MentionPreferencesSavedData::new,
                        MentionPreferencesSavedData::load,
                        null
                ),
                CallYouByYourName.MODID + "_mention_prefs"
        );
    }

    private static @NotNull MentionPreferencesSavedData load(
            @NotNull CompoundTag tag,
            @NotNull HolderLookup.Provider registries
    ) {
        MentionPreferencesSavedData data = new MentionPreferencesSavedData();
        if (!tag.contains(TAG_PREFS, Tag.TAG_LIST)) {
            return data;
        }
        ListTag list = tag.getList(TAG_PREFS, Tag.TAG_COMPOUND);
        RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, registries);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            if (!entry.hasUUID(TAG_PLAYER) || !entry.contains(TAG_DATA, Tag.TAG_COMPOUND)) {
                continue;
            }
            UUID playerId = entry.getUUID(TAG_PLAYER);
            CompoundTag prefsTag = entry.getCompound(TAG_DATA);
            MentionPreferences.CODEC.parse(ops, prefsTag)
                    .resultOrPartial(CallYouByYourName.LOGGER::error)
                    .ifPresent(prefs -> {
                        MentionPreferences copy = new MentionPreferences();
                        copy.copyFrom(prefs);
                        data.preferencesByPlayer.put(playerId, copy);
                    });
        }
        return data;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        if (preferencesByPlayer.isEmpty()) {
            return tag;
        }
        RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, registries);
        ListTag list = new ListTag();
        for (Map.Entry<UUID, MentionPreferences> entry : preferencesByPlayer.entrySet()) {
            UUID playerId = entry.getKey();
            MentionPreferences prefs = entry.getValue();
            MentionPreferences.CODEC.encodeStart(ops, prefs)
                    .resultOrPartial(CallYouByYourName.LOGGER::error)
                    .ifPresent(encoded -> {
                        if (encoded instanceof CompoundTag prefsTag) {
                            CompoundTag record = new CompoundTag();
                            record.putUUID(TAG_PLAYER, playerId);
                            record.put(TAG_DATA, prefsTag);
                            list.add(record);
                        }
                    });
        }
        if (!list.isEmpty()) {
            tag.put(TAG_PREFS, list);
        }
        return tag;
    }

    public void update(@NotNull UUID playerId, @NotNull MentionPreferences preferences) {
        MentionPreferences copy = new MentionPreferences();
        copy.copyFrom(preferences);
        preferencesByPlayer.put(playerId, copy);
        setDirty();
    }

    public @NotNull Optional<MentionPreferences> find(@NotNull UUID playerId) {
        MentionPreferences stored = preferencesByPlayer.get(playerId);
        if (stored == null) {
            return Optional.empty();
        }
        MentionPreferences copy = new MentionPreferences();
        copy.copyFrom(stored);
        return Optional.of(copy);
    }
}
