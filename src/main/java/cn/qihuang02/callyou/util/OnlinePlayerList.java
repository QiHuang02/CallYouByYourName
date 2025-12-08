package cn.qihuang02.callyou.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class OnlinePlayerList {
    private final List<UUID> onlinePlayerUUIDs = new ArrayList<>();
    private final Map<UUID, String> nameCache = new HashMap<>();

    public void clear() {
        onlinePlayerUUIDs.clear();
        nameCache.clear();
    }

    public void refreshFromServer(@NotNull MinecraftServer server) {
        onlinePlayerUUIDs.clear();
        nameCache.clear();

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            UUID uuid = player.getUUID();
            onlinePlayerUUIDs.add(uuid);
            nameCache.put(uuid, player.getGameProfile().getName());
        }
    }

    public void onPlayerLoggedIn(@NotNull ServerPlayer player) {
        UUID uuid = player.getUUID();
        if (!onlinePlayerUUIDs.contains(uuid)) {
            onlinePlayerUUIDs.add(uuid);
        }
        nameCache.put(uuid, player.getGameProfile().getName());
    }

    public void onPlayerLoggedOut(@NotNull ServerPlayer player) {
        UUID uuid = player.getUUID();
        onlinePlayerUUIDs.remove(uuid);
        nameCache.remove(uuid);
    }

    public List<UUID> getOnlinePlayerUUIDs() {
        return Collections.unmodifiableList(onlinePlayerUUIDs);
    }

    public UUID findOnlinePlayerByExactName(@NotNull MinecraftServer server, @NotNull String name) {
        String target = name.toLowerCase(Locale.ROOT);
        for (UUID uuid : onlinePlayerUUIDs) {
            String cachedName = getPlayerName(server, uuid);
            if (cachedName != null && !cachedName.isEmpty()
                    && cachedName.toLowerCase(Locale.ROOT).equals(target)) {
                return uuid;
            }
        }
        return null;
    }

    public String getPlayerName(@NotNull MinecraftServer server, @NotNull UUID uuid) {
        ServerPlayer player = server.getPlayerList().getPlayer(uuid);
        if (player != null) {
            String name = player.getGameProfile().getName();
            nameCache.put(uuid, name);
            return name;
        }
        return nameCache.getOrDefault(uuid, "Unknown");
    }

    public List<UUID> findOnlinePlayersByName(@NotNull MinecraftServer server, @NotNull String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        return server.getPlayerList().getPlayers().stream()
                .filter(p -> p.getGameProfile().getName().toLowerCase(Locale.ROOT).startsWith(lower))
                .map(ServerPlayer::getUUID)
                .collect(Collectors.toList());
    }

    public List<UUID> getOnlinePlayersSorted(@NotNull MinecraftServer server) {
        return onlinePlayerUUIDs.stream()
                .sorted(Comparator.comparing(uuid -> getPlayerName(server, uuid), String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    public List<String> getOnlinePlayerNamesSorted(@NotNull MinecraftServer server) {
        return getOnlinePlayersSorted(server).stream()
                .map(uuid -> getPlayerName(server, uuid))
                .toList();
    }

    public static @NotNull List<String> getClientOnlinePlayerNames(@NotNull Minecraft minecraft) {
        ClientPacketListener connection = minecraft.getConnection();
        LocalPlayer localPlayer = minecraft.player;
        if (connection == null || localPlayer == null) {
            return List.of();
        }

        String selfName = localPlayer.getGameProfile().getName();
        List<String> result = new ArrayList<>();

        for (PlayerInfo info : connection.getOnlinePlayers()) {
            String name = info.getProfile().getName();
            if (name == null || name.isEmpty()) {
                continue;
            }
            if (name.equals(selfName)) {
                continue;
            }
            result.add(name);
        }

        return result.stream()
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }
}