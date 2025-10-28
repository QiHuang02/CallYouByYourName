package cn.qihuang02.cyyn.server.chat;

import cn.qihuang02.cyyn.CallYouByYourName;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber(modid = CallYouByYourName.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CYYNServerChatEvent {
    private static MentionChatProcessor processor;

    @SubscribeEvent
    public static void onServerChat(@NotNull ServerChatEvent event) {
        if (getProcessor().process(event)) {
            event.setCanceled(true);
        }
    }

    @NotNull
    private static MentionChatProcessor getProcessor() {
        if (processor == null) {
            processor = MentionChatProcessor.createDefault();
        }
        return processor;
    }
}
