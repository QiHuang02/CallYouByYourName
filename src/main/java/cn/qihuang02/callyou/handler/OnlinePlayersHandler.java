package cn.qihuang02.callyou.handler;

import cn.qihuang02.callyou.CallYouByYourName;
import cn.qihuang02.callyou.core.MentionExecutor;
import cn.qihuang02.callyou.util.OnlinePlayerList;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(modid = CallYouByYourName.MODID)
public final class OnlinePlayersHandler {
    private static final OnlinePlayerList ONLINE_PLAYERS = new OnlinePlayerList();

    public static @NotNull OnlinePlayerList getOnlinePlayers() {
        return ONLINE_PLAYERS;
    }

    @SubscribeEvent
    public static void onServerStarted(@NotNull ServerStartedEvent event) {
        ONLINE_PLAYERS.refreshFromServer(event.getServer());
    }

    @SubscribeEvent
    public static void onServerStopped(@NotNull ServerStoppedEvent event) {
        ONLINE_PLAYERS.clear();
    }

    @SubscribeEvent
    public static void onPlayerLogin(@NotNull PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ONLINE_PLAYERS.onPlayerLoggedIn(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(@NotNull PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ONLINE_PLAYERS.onPlayerLoggedOut(player);
            MentionExecutor.handlePlayerLogout(player);
        }
    }
}
