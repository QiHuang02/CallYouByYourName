package cn.qihuang02.callyou.core.handler;

import cn.qihuang02.callyou.CallYouByYourName;
import cn.qihuang02.callyou.core.MentionExecutor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(modid = CallYouByYourName.MODID)
public final class MentionChatHandler {
    @SubscribeEvent
    public static void onServerChat(@NotNull ServerChatEvent event) {
        MentionExecutor.handleChatEvent(event);
    }
}
