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
    private final PrefixIndex prefixIndex = new PrefixIndex();

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
        prefixIndex.clear();
    }

    public void refreshFromServer(@NotNull MinecraftServer server) {
        clear();

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            cacheOnlinePlayer(player.getUUID(), player.getGameProfile().getName());
        }
    }

    public void onPlayerLoggedIn(@NotNull ServerPlayer player) {
        cacheOnlinePlayer(player.getUUID(), player.getGameProfile().getName());
    }

    public void onPlayerLoggedOut(@NotNull ServerPlayer player) {
        removeOnlinePlayer(player.getUUID(), player.getGameProfile().getName());
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
        if (lower.isEmpty()) {
            return new ArrayList<>(onlinePlayerUUIDs);
        }
        return prefixIndex.find(lower);
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

    private void cacheOnlinePlayer(@NotNull UUID uuid, String name) {
        if (!onlinePlayerUUIDs.contains(uuid)) {
            onlinePlayerUUIDs.add(uuid);
        }
        nameCache.put(uuid, name);

        if (name != null && !name.isEmpty()) {
            String lower = name.toLowerCase(Locale.ROOT);
            nameToUUID.put(lower, uuid);
            prefixIndex.insert(lower, uuid);
        }
    }

    private void removeOnlinePlayer(@NotNull UUID uuid, String name) {
        onlinePlayerUUIDs.remove(uuid);
        String cachedName = nameCache.remove(uuid);
        String resolvedName = cachedName != null ? cachedName : name;

        if (resolvedName != null && !resolvedName.isEmpty()) {
            String lower = resolvedName.toLowerCase(Locale.ROOT);
            nameToUUID.remove(lower);
            prefixIndex.remove(lower, uuid);
        }
    }

    /**
     * Prefix tree storing online player UUIDs for fast prefix lookups.
     */
    private static final class PrefixIndex {
        private final TrieNode root = new TrieNode();

        void insert(@NotNull String name, @NotNull UUID uuid) {
            if (name.isEmpty()) {
                return;
            }

            TrieNode node = root;
            for (char c : name.toCharArray()) {
                node = node.children.computeIfAbsent(c, ignored -> new TrieNode());
                node.uuids.add(uuid);
            }
        }

        void remove(@NotNull String name, @NotNull UUID uuid) {
            if (name.isEmpty()) {
                return;
            }

            TrieNode node = root;
            Deque<PathEntry> path = new ArrayDeque<>();

            for (char c : name.toCharArray()) {
                TrieNode next = node.children.get(c);
                if (next == null) {
                    return;
                }
                path.push(new PathEntry(node, next, c));
                node = next;
            }

            for (PathEntry entry : path) {
                entry.child.uuids.remove(uuid);
            }

            while (!path.isEmpty()) {
                PathEntry entry = path.pop();
                TrieNode child = entry.child;
                if (child.uuids.isEmpty() && child.children.isEmpty()) {
                    entry.parent.children.remove(entry.edge);
                }
            }
        }

        @NotNull
        List<UUID> find(@NotNull String prefix) {
            if (prefix.isEmpty()) {
                return List.of();
            }

            TrieNode node = root;
            for (char c : prefix.toCharArray()) {
                node = node.children.get(c);
                if (node == null) {
                    return List.of();
                }
            }

            if (node.uuids.isEmpty()) {
                return List.of();
            }

            return new ArrayList<>(node.uuids);
        }

        void clear() {
            root.children.clear();
            root.uuids.clear();
        }

        private static final class TrieNode {
            private final Map<Character, TrieNode> children = new HashMap<>();
            private final Set<UUID> uuids = new LinkedHashSet<>();
        }

        private record PathEntry(TrieNode parent, TrieNode child, char edge) {
        }
    }
}
