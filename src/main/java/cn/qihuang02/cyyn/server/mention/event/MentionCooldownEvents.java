package cn.qihuang02.cyyn.server.mention.event;

import cn.qihuang02.cyyn.CallYouByYourName;
import cn.qihuang02.cyyn.server.mention.MentionCooldownTracker;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber(modid = CallYouByYourName.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MentionCooldownEvents {
    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.@NotNull PlayerLoggedOutEvent event) {
        MentionCooldownTracker.getInstance().clearCooldown(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        MentionCooldownTracker.getInstance().clearAllCooldowns();
    }
}
