package cn.qihuang02.callyou;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = CallYouByYourName.MODID, value = Dist.CLIENT)
public final class CallYouByYourNameClient {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        CallYouByYourName.LOGGER.debug("CallYou client setup complete.");
    }
}
