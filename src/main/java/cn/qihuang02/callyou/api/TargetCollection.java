package cn.qihuang02.callyou.api;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

public final class TargetCollection {
    private static final TargetCollection EMPTY = new TargetCollection(List.of(), List.of());

    private final List<UUID> allIds;
    private final List<ServerPlayer> preResolved;

    private TargetCollection(@NotNull List<UUID> allIds, @NotNull List<ServerPlayer> preResolved) {
        this.allIds = List.copyOf(allIds);
        this.preResolved = List.copyOf(preResolved);
    }

    public static @NotNull TargetCollection empty() {
        return EMPTY;
    }

    public static @NotNull TargetCollection ofPlayers(@NotNull List<ServerPlayer> players) {
        if (players.isEmpty()) {
            return EMPTY;
        }
        LinkedHashSet<UUID> ids = new LinkedHashSet<>();
        List<ServerPlayer> resolved = new ArrayList<>(players.size());
        for (ServerPlayer player : players) {
            if (player == null) {
                continue;
            }
            UUID id = player.getUUID();
            if (ids.add(id)) {
                resolved.add(player);
            }
        }
        if (ids.isEmpty()) {
            return EMPTY;
        }
        return new TargetCollection(List.copyOf(ids), List.copyOf(resolved));
    }

    public static @NotNull TargetCollection ofIds(@NotNull List<UUID> ids) {
        if (ids.isEmpty()) {
            return EMPTY;
        }
        LinkedHashSet<UUID> unique = new LinkedHashSet<>();
        for (UUID id : ids) {
            if (id != null) {
                unique.add(id);
            }
        }
        if (unique.isEmpty()) {
            return EMPTY;
        }
        return new TargetCollection(List.copyOf(unique), List.of());
    }

    public @NotNull @Unmodifiable List<UUID> allIds() {
        return allIds;
    }

    public boolean isEmpty() {
        return allIds.isEmpty();
    }

    public @NotNull @Unmodifiable List<ServerPlayer> resolveOnline(@NotNull MinecraftServer server) {
        if (!preResolved.isEmpty()) {
            return preResolved;
        }
        List<ServerPlayer> resolved = new ArrayList<>();
        for (UUID id : allIds) {
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (player != null) {
                resolved.add(player);
            }
        }
        return List.copyOf(resolved);
    }

    public @NotNull TargetCollection removeIf(@NotNull Predicate<UUID> predicate) {
        if (allIds.isEmpty()) {
            return this;
        }
        List<UUID> kept = new ArrayList<>(allIds.size());
        for (UUID id : allIds) {
            if (id != null && !predicate.test(id)) {
                kept.add(id);
            }
        }
        if (kept.size() == allIds.size()) {
            return this;
        }
        if (kept.isEmpty()) {
            return EMPTY;
        }
        Set<UUID> keepSet = new HashSet<>(kept);
        List<ServerPlayer> resolved = new ArrayList<>(preResolved.size());
        for (ServerPlayer player : preResolved) {
            if (keepSet.contains(player.getUUID())) {
                resolved.add(player);
            }
        }
        return new TargetCollection(kept, resolved);
    }
}
