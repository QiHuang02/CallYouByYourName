package cn.qihuang02.cyyn.mention;

import cn.qihuang02.cyyn.CallYouByYourName;
import cn.qihuang02.cyyn.network.CYYNMessages;
import cn.qihuang02.cyyn.network.packet.SyncMentionGroupsPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class MentionGroupSynchronizer {
    private static boolean initialized;

    public static synchronized void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        CallYouByYourName.LOGGER.info("Initializing MentionGroupSynchronizer");
        MentionGroupRegistry.addListener(MentionGroupSynchronizer::broadcastToPlayers);
        MinecraftForge.EVENT_BUS.addListener(MentionGroupSynchronizer::handlePlayerLoggedIn);
    }

    private static void broadcastToPlayers(@NotNull Collection<String> tokens) {
        if (ServerLifecycleHooks.getCurrentServer() == null) {
            return;
        }
        CallYouByYourName.LOGGER.info("Broadcasting mention group tokens to all players: {}", tokens);
        CYYNMessages.getChannel().send(PacketDistributor.ALL.noArg(), new SyncMentionGroupsPacket(tokens));
    }

    private static void handlePlayerLoggedIn(PlayerEvent.@NotNull PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }
        CallYouByYourName.LOGGER.info("Synchronizing mention group tokens to player {}", serverPlayer.getScoreboardName());
        CYYNMessages.getChannel().send(PacketDistributor.PLAYER.with(() -> serverPlayer), new SyncMentionGroupsPacket(MentionGroupRegistry.tokens()));
    }
}
