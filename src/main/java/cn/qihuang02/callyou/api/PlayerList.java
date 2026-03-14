package cn.qihuang02.callyou.api;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public interface PlayerList {
    void clear();

    void refreshFromServer(@NotNull MinecraftServer server);

    void onPlayerLoggedIn(@NotNull ServerPlayer player);

    void onPlayerLoggedOut(@NotNull ServerPlayer player);

    @NotNull List<UUID> getPlayerUUIDs();

    @Nullable UUID findPlayerByExactName(@NotNull MinecraftServer server, @NotNull String name);

    @NotNull String getPlayerName(@NotNull MinecraftServer server, @NotNull UUID uuid);

    @NotNull List<UUID> findPlayersByName(@NotNull MinecraftServer server, @NotNull String name);

    @NotNull List<UUID> getPlayersSorted(@NotNull MinecraftServer server);

    @NotNull List<String> getPlayerNamesSorted(@NotNull MinecraftServer server);
}
