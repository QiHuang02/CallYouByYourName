package cn.qihuang02.callyou;

import cn.qihuang02.callyou.core.client.chat.ClientMentionCompensator;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber(modid = CallYouByYourName.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CallYouByYourNameClient {

    @SubscribeEvent
    public static void onClientChatReceived(@NotNull ClientChatReceivedEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return;
        }
        var message = event.getMessage();
        var compensated = ClientMentionCompensator.compensate(minecraft, message);
        if (compensated != message) {
            event.setMessage(compensated);
        }
    }
}
