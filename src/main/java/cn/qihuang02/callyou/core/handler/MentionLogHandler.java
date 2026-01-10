package cn.qihuang02.callyou.core.handler;

import cn.qihuang02.callyou.CallYouByYourName;
import cn.qihuang02.callyou.config.CallYouConfig;
import cn.qihuang02.callyou.core.saveddata.MentionSavedData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(modid = CallYouByYourName.MODID)
public final class MentionLogHandler {
    @SubscribeEvent
    public static void onPlayerLogin(@NotNull PlayerEvent.PlayerLoggedInEvent event) {
        if (!CallYouConfig.COMMON.enableServerSideHistory.get()) {
            return;
        }
        if (event.getEntity() instanceof ServerPlayer player) {
            MentionSavedData data = MentionSavedData.get(player.serverLevel());
            data.pruneOldLogs();
            int unread = data.countUnread(player.getUUID());
            if (unread > 0) {
                player.sendSystemMessage(
                        Component.translatable("message.callyou.unread_mentions", unread)
                );
            }
        }
    }
}
