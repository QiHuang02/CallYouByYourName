package cn.qihuang02.callyou.core.attachment;

import cn.qihuang02.callyou.core.saveddata.MentionPreferencesSavedData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class that provides access to MentionPreferences through an in-memory cache
 * backed by SavedData persistence. Replaces NeoForge's AttachmentType system.
 */
public final class CallYouAttachments {
    private static final Map<UUID, MentionPreferences> ONLINE_CACHE = new ConcurrentHashMap<>();

    private CallYouAttachments() {}

    /**
     * Gets preferences for an online player, using the in-memory cache.
     */
    public static @NotNull MentionPreferences getPreferences(@NotNull ServerPlayer player) {
        return ONLINE_CACHE.computeIfAbsent(player.getUUID(), id -> {
            MinecraftServer server = player.getServer();
            if (server != null) {
                MentionPreferencesSavedData data = MentionPreferencesSavedData.get(player.serverLevel());
                return data.getPreferences(id);
            }
            return new MentionPreferences();
        });
    }

    /**
     * Gets preferences for a player by UUID, using the cache if online or falling back to SavedData.
     */
    public static @NotNull MentionPreferences getPreferences(@NotNull MinecraftServer server, @NotNull UUID playerId) {
        MentionPreferences cached = ONLINE_CACHE.get(playerId);
        if (cached != null) {
            return cached;
        }
        MentionPreferencesSavedData data = MentionPreferencesSavedData.get(server.overworld());
        return data.getPreferences(playerId);
    }

    /**
     * Sets preferences for an online player, updating both cache and SavedData.
     */
    public static void setPreferences(@NotNull ServerPlayer player, @NotNull MentionPreferences prefs) {
        ONLINE_CACHE.put(player.getUUID(), prefs);
        MinecraftServer server = player.getServer();
        if (server != null) {
            MentionPreferencesSavedData data = MentionPreferencesSavedData.get(player.serverLevel());
            data.updatePreferences(player.getUUID(), prefs);
        }
    }

    /**
     * Called when a player logs in. Loads their preferences from SavedData into the cache.
     */
    public static void onPlayerLogin(@NotNull ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server != null) {
            MentionPreferencesSavedData data = MentionPreferencesSavedData.get(player.serverLevel());
            MentionPreferences prefs = data.getPreferences(player.getUUID());
            ONLINE_CACHE.put(player.getUUID(), prefs);
        }
    }

    /**
     * Called when a player logs out. Saves their preferences to SavedData and removes from cache.
     */
    public static void onPlayerLogout(@NotNull ServerPlayer player) {
        UUID playerId = player.getUUID();
        MentionPreferences prefs = ONLINE_CACHE.remove(playerId);
        if (prefs != null) {
            MinecraftServer server = player.getServer();
            if (server != null) {
                MentionPreferencesSavedData data = MentionPreferencesSavedData.get(player.serverLevel());
                data.updatePreferences(playerId, prefs);
            }
        }
    }
}
