package cn.qihuang02.callyou.core.handler;

import cn.qihuang02.callyou.CallYouByYourName;
import cn.qihuang02.callyou.core.attachment.CallYouAttachments;
import cn.qihuang02.callyou.core.network.NetworkHandler;
import cn.qihuang02.callyou.core.saveddata.MentionPreferencesSavedData;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(modid = CallYouByYourName.MODID)
public final class MentionPreferencesHandler {
    @SubscribeEvent
    public static void onPlayerLogin(@NotNull PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            NetworkHandler.syncPreferences(player);
            MentionPreferencesSavedData.get(player.serverLevel())
                    .update(player.getUUID(), player.getData(CallYouAttachments.MENTION_PREFERENCES.get()));
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(@NotNull PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            NetworkHandler.syncPreferences(player);
            MentionPreferencesSavedData.get(player.serverLevel())
                    .update(player.getUUID(), player.getData(CallYouAttachments.MENTION_PREFERENCES.get()));
        }
    }
}
