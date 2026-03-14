package cn.qihuang02.callyou.core.handler;

import cn.qihuang02.callyou.core.mention.MentionExecutor;
import cn.qihuang02.callyou.util.OfflinePlayerList;
import cn.qihuang02.callyou.util.OnlinePlayerList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public final class OnlinePlayersHandler {
    private static final OnlinePlayerList ONLINE_PLAYERS = new OnlinePlayerList();
    private static final OfflinePlayerList OFFLINE_PLAYERS = new OfflinePlayerList();

    public static @NotNull OnlinePlayerList getOnlinePlayers() {
        return ONLINE_PLAYERS;
    }

    public static @NotNull OfflinePlayerList getOfflinePlayers() {
        return OFFLINE_PLAYERS;
    }

    @SubscribeEvent
    public static void onServerStarted(@NotNull ServerStartedEvent event) {
        ONLINE_PLAYERS.refreshFromServer(event.getServer());
        OFFLINE_PLAYERS.refreshFromServer(event.getServer());
    }

    @SubscribeEvent
    public static void onServerStopped(@NotNull ServerStoppedEvent event) {
        ONLINE_PLAYERS.clear();
        OFFLINE_PLAYERS.clear();
    }

    @SubscribeEvent
    public static void onPlayerLogin(@NotNull PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ONLINE_PLAYERS.onPlayerLoggedIn(player);
            OFFLINE_PLAYERS.onPlayerLoggedIn(player);
            MentionExecutor.handlePlayerLogin(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(@NotNull PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ONLINE_PLAYERS.onPlayerLoggedOut(player);
            OFFLINE_PLAYERS.onPlayerLoggedOut(player);
            MentionExecutor.handlePlayerLogout(player);
        }
    }
}
