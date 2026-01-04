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
    private static ClientPacketListener cachedClientConnection;
    private static List<String> cachedClientNames = List.of();
    private static int cachedOnlinePlayerCount = -1;
    private static String cachedSelfName = "";

    private final List<UUID> onlinePlayerUUIDs = new ArrayList<>();
    private final Map<UUID, String> nameCache = new HashMap<>();
    private final Map<String, UUID> nameToUUID = new HashMap<>();

    public static @NotNull List<String> getClientOnlinePlayerNames(@NotNull Minecraft minecraft) {
        ClientPacketListener connection = minecraft.getConnection();
        LocalPlayer localPlayer = minecraft.player;
        if (connection == null || localPlayer == null) {
            resetClientCache();
            return List.of();
        }

        if (connection != cachedClientConnection) {
            resetClientCache();
            cachedClientConnection = connection;
        }

        String selfName = localPlayer.getGameProfile().getName();
        int onlinePlayerCount = connection.getOnlinePlayers().size();

        if (!cachedClientNames.isEmpty()
                && onlinePlayerCount == cachedOnlinePlayerCount
                && selfName.equals(cachedSelfName)) {
            return cachedClientNames;
        }

        List<String> result = new ArrayList<>(onlinePlayerCount);

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

        cachedClientNames = result.stream()
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();

        cachedOnlinePlayerCount = onlinePlayerCount;
        cachedSelfName = selfName;

        return cachedClientNames;
    }

    private static void resetClientCache() {
        cachedClientNames = List.of();
        cachedClientConnection = null;
        cachedOnlinePlayerCount = -1;
        cachedSelfName = "";
    }

    public void clear() {
        onlinePlayerUUIDs.clear();
        nameCache.clear();
        nameToUUID.clear();
    }

    public void refreshFromServer(@NotNull MinecraftServer server) {
        onlinePlayerUUIDs.clear();
        nameCache.clear();
        nameToUUID.clear();

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            UUID uuid = player.getUUID();
            String name = player.getGameProfile().getName();
            onlinePlayerUUIDs.add(uuid);
            nameCache.put(uuid, name);
            if (name != null) {
                nameToUUID.put(name.toLowerCase(Locale.ROOT), uuid);
            }
        }
    }

    public void onPlayerLoggedIn(@NotNull ServerPlayer player) {
        UUID uuid = player.getUUID();
        String name = player.getGameProfile().getName();
        if (!onlinePlayerUUIDs.contains(uuid)) {
            onlinePlayerUUIDs.add(uuid);
        }
        nameCache.put(uuid, name);
        if (name != null) {
            nameToUUID.put(name.toLowerCase(Locale.ROOT), uuid);
        }
    }

    public void onPlayerLoggedOut(@NotNull ServerPlayer player) {
        UUID uuid = player.getUUID();
        String name = nameCache.get(uuid);
        onlinePlayerUUIDs.remove(uuid);
        nameCache.remove(uuid);
        if (name != null) {
            nameToUUID.remove(name.toLowerCase(Locale.ROOT));
        }
    }

    public List<UUID> getOnlinePlayerUUIDs() {
        return Collections.unmodifiableList(onlinePlayerUUIDs);
    }

    public UUID findOnlinePlayerByExactName(@NotNull MinecraftServer server, @NotNull String name) {
        return nameToUUID.get(name.toLowerCase(Locale.ROOT));
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
}