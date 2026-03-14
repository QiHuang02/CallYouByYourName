package cn.qihuang02.callyou.core.handler;

import cn.qihuang02.callyou.core.mention.MentionExecutor;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public final class MentionChatHandler {
    @SubscribeEvent
    public static void onServerChat(@NotNull ServerChatEvent event) {
        MentionExecutor.handleChatEvent(event);
    }
}
