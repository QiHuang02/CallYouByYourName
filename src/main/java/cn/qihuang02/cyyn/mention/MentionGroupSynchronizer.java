package cn.qihuang02.cyyn.mention;

import cn.qihuang02.cyyn.network.CYYNMessages;
import cn.qihuang02.cyyn.network.ClientboundSyncMentionGroupsPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class MentionGroupSynchronizer {
    public static void init() {
        MentionGroupRegistry.addListener(MentionGroupSynchronizer::broadcastToPlayers);
        MinecraftForge.EVENT_BUS.addListener(MentionGroupSynchronizer::handlePlayerLoggedIn);
    }

    private static void broadcastToPlayers(@NotNull Collection<String> tokens) {
        if (ServerLifecycleHooks.getCurrentServer() == null) {
            return;
        }
        CYYNMessages.getChannel().send(PacketDistributor.ALL.noArg(), new ClientboundSyncMentionGroupsPacket(tokens));
    }

    private static void handlePlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }
        CYYNMessages.getChannel().send(PacketDistributor.PLAYER.with(() -> serverPlayer), new ClientboundSyncMentionGroupsPacket(MentionGroupRegistry.tokens()));
    }
}
