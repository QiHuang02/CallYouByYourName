package cn.qihuang02.callyou;

import cn.qihuang02.callyou.core.client.chat.ClientMentionCompensator;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import org.jetbrains.annotations.NotNull;

@Mod(value = CallYouByYourName.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = CallYouByYourName.MODID, value = Dist.CLIENT)
public class CallYouByYourNameClient {
    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        CallYouByYourName.LOGGER.info("HELLO FROM CLIENT SETUP");
        CallYouByYourName.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }

    @SubscribeEvent
    static void onClientChatReceived(@NotNull ClientChatReceivedEvent event) {
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
