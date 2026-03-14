package cn.qihuang02.callyou.core.saveddata;

import cn.qihuang02.callyou.CallYouByYourName;
import cn.qihuang02.callyou.core.attachment.MentionPreferences;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.Util;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class MentionPreferencesSavedData extends SavedData {
    private static final String TAG_PREFERENCES = "preferences";
    private static final String TAG_PLAYER = "player";
    private static final String TAG_PREFS = "prefs";

    private static final Codec<StoredPreference> STORED_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.optionalFieldOf(TAG_PLAYER, Util.NIL_UUID).forGetter(StoredPreference::playerId),
            MentionPreferences.CODEC.optionalFieldOf(TAG_PREFS, new MentionPreferences())
                    .forGetter(StoredPreference::preferences)
    ).apply(instance, StoredPreference::new));

    private final Map<UUID, MentionPreferences> preferencesByPlayer = new HashMap<>();

    public MentionPreferencesSavedData() {
    }

    public static @NotNull MentionPreferencesSavedData get(@NotNull ServerLevel level) {
        ServerLevel overworld = level.getServer().overworld();
        if (overworld == null) {
            throw new IllegalStateException("Overworld level is not available for mention preferences storage");
        }
        return overworld.getDataStorage().computeIfAbsent(
                MentionPreferencesSavedData::load,
                MentionPreferencesSavedData::new,
                CallYouByYourName.MODID + "_mention_prefs"
        );
    }

    private static @NotNull MentionPreferencesSavedData load(@NotNull CompoundTag tag) {
        MentionPreferencesSavedData data = new MentionPreferencesSavedData();
        if (!tag.contains(TAG_PREFERENCES, Tag.TAG_LIST)) {
            return data;
        }
        ListTag list = tag.getList(TAG_PREFERENCES, Tag.TAG_COMPOUND);
        if (list.isEmpty()) {
            return data;
        }
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            STORED_CODEC.parse(NbtOps.INSTANCE, entry)
                    .resultOrPartial(CallYouByYourName.LOGGER::error)
                    .ifPresent(stored -> {
                        UUID playerId = stored.playerId();
                        if (playerId == null || Util.NIL_UUID.equals(playerId)) {
                            return;
                        }
                        data.preferencesByPlayer.put(playerId, copyOf(stored.preferences()));
                    });
        }
        return data;
    }

    private static @NotNull MentionPreferences copyOf(@NotNull MentionPreferences preferences) {
        MentionPreferences copy = new MentionPreferences();
        copy.copyFrom(preferences);
        return copy;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        if (preferencesByPlayer.isEmpty()) {
            return tag;
        }
        ListTag list = new ListTag();
        for (Map.Entry<UUID, MentionPreferences> entry : preferencesByPlayer.entrySet()) {
            StoredPreference stored = new StoredPreference(entry.getKey(), entry.getValue());
            STORED_CODEC.encodeStart(NbtOps.INSTANCE, stored)
                    .resultOrPartial(CallYouByYourName.LOGGER::error)
                    .ifPresent(encoded -> {
                        if (encoded instanceof CompoundTag compound) {
                            list.add(compound);
                        }
                    });
        }
        if (!list.isEmpty()) {
            tag.put(TAG_PREFERENCES, list);
        }
        return tag;
    }

    public @NotNull MentionPreferences getPreferences(@NotNull UUID playerId) {
        MentionPreferences stored = preferencesByPlayer.get(playerId);
        if (stored == null) {
            return new MentionPreferences();
        }
        return copyOf(stored);
    }

    public void updatePreferences(@NotNull UUID playerId, @NotNull MentionPreferences preferences) {
        if (playerId == null || Util.NIL_UUID.equals(playerId)) {
            return;
        }
        preferencesByPlayer.put(playerId, copyOf(preferences));
        setDirty();
    }

    private record StoredPreference(@NotNull UUID playerId, @NotNull MentionPreferences preferences) {
    }
}
