package cn.qihuang02.callyou.util;

import cn.qihuang02.callyou.api.PlayerList;
import cn.qihuang02.callyou.core.saveddata.KnownPlayerSavedData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class OfflinePlayerList implements PlayerList {
    private final Map<UUID, String> nameCache = new HashMap<>();
    private final Map<String, UUID> nameToUUID = new HashMap<>();

    @Override
    public void clear() {
        nameCache.clear();
        nameToUUID.clear();
    }

    @Override
    public void refreshFromServer(@NotNull MinecraftServer server) {
        clear();
        ServerLevel overworld = server.overworld();
        if (overworld == null) {
            return;
        }
        KnownPlayerSavedData data = KnownPlayerSavedData.get(overworld);
        for (Map.Entry<UUID, String> entry : data.getPlayers().entrySet()) {
            cacheOfflinePlayer(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void onPlayerLoggedIn(@NotNull ServerPlayer player) {
        KnownPlayerSavedData data = KnownPlayerSavedData.get(player.serverLevel());
        data.remove(player.getUUID());
        removeOfflinePlayer(player.getUUID(), player.getGameProfile().getName());
    }

    @Override
    public void onPlayerLoggedOut(@NotNull ServerPlayer player) {
        String name = player.getGameProfile().getName();
        if (name == null || name.isBlank()) {
            return;
        }
        KnownPlayerSavedData data = KnownPlayerSavedData.get(player.serverLevel());
        data.put(player.getUUID(), name);
        cacheOfflinePlayer(player.getUUID(), name);
    }

    @Override
    public @NotNull List<UUID> getPlayerUUIDs() {
        return Collections.unmodifiableList(new ArrayList<>(nameCache.keySet()));
    }

    @Override
    public UUID findPlayerByExactName(@NotNull MinecraftServer server, @NotNull String name) {
        return nameToUUID.get(name.toLowerCase(Locale.ROOT));
    }

    @Override
    public @NotNull String getPlayerName(@NotNull MinecraftServer server, @NotNull UUID uuid) {
        return nameCache.getOrDefault(uuid, "Unknown");
    }

    @Override
    public @NotNull List<UUID> findPlayersByName(@NotNull MinecraftServer server, @NotNull String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        if (lower.isEmpty()) {
            return new ArrayList<>(nameCache.keySet());
        }
        List<UUID> result = new ArrayList<>();
        for (Map.Entry<UUID, String> entry : nameCache.entrySet()) {
            String cachedName = entry.getValue();
            if (cachedName == null) {
                continue;
            }
            if (cachedName.toLowerCase(Locale.ROOT).startsWith(lower)) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    @Override
    public @NotNull List<UUID> getPlayersSorted(@NotNull MinecraftServer server) {
        return nameCache.keySet().stream()
                .sorted(Comparator.comparing(uuid -> getPlayerName(server, uuid), String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    @Override
    public @NotNull List<String> getPlayerNamesSorted(@NotNull MinecraftServer server) {
        return getPlayersSorted(server).stream()
                .map(uuid -> getPlayerName(server, uuid))
                .toList();
    }

    private void cacheOfflinePlayer(@NotNull UUID uuid, String name) {
        if (name == null || name.isBlank()) {
            return;
        }
        nameCache.put(uuid, name);
        nameToUUID.put(name.toLowerCase(Locale.ROOT), uuid);
    }

    private void removeOfflinePlayer(@NotNull UUID uuid, String nameHint) {
        String cachedName = nameCache.remove(uuid);
        String resolvedName = cachedName != null ? cachedName : nameHint;
        if (resolvedName == null || resolvedName.isBlank()) {
            return;
        }
        nameToUUID.remove(resolvedName.toLowerCase(Locale.ROOT));
    }
}
