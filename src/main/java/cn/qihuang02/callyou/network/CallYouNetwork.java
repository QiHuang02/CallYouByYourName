package cn.qihuang02.callyou.network;

import cn.qihuang02.callyou.CallYouByYourName;
import cn.qihuang02.callyou.core.attachment.CallYouAttachments;
import cn.qihuang02.callyou.core.attachment.MentionPreferences;
import cn.qihuang02.callyou.client.ClientMentionPreferences;
import cn.qihuang02.callyou.client.ToastNotifierClient;
import cn.qihuang02.callyou.network.payload.MentionPreferencesRequestPayload;
import cn.qihuang02.callyou.network.payload.MentionPreferencesSyncPayload;
import cn.qihuang02.callyou.network.payload.MentionPreferencesUpdatePayload;
import cn.qihuang02.callyou.network.payload.MentionToastPayload;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(modid = CallYouByYourName.MODID)
public final class CallYouNetwork {
    private static final String VERSION = "1.0";

    @SubscribeEvent
    public static void register(final @NotNull RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(CallYouByYourName.MODID).versioned(VERSION);

        registrar.playToClient(
                MentionPreferencesSyncPayload.TYPE,
                MentionPreferencesSyncPayload.STREAM_CODEC,
                CallYouNetwork::handleSync
        );

        registrar.playToServer(
                MentionPreferencesUpdatePayload.TYPE,
                MentionPreferencesUpdatePayload.STREAM_CODEC,
                CallYouNetwork::handleUpdate
        );

        registrar.playToServer(
                MentionPreferencesRequestPayload.TYPE,
                MentionPreferencesRequestPayload.STREAM_CODEC,
                CallYouNetwork::handleRequest
        );

        registrar.playToClient(
                MentionToastPayload.TYPE,
                MentionToastPayload.STREAM_CODEC,
                CallYouNetwork::handleToast
        );
    }

    public static void syncPreferences(@NotNull ServerPlayer player) {
        MentionPreferences preferences = player.getData(CallYouAttachments.MENTION_PREFERENCES.get());

        PacketDistributor.sendToPlayer(player, new MentionPreferencesSyncPayload(preferences));
    }

    public static void sendToast(@NotNull ServerPlayer player, @NotNull AdvancementHolder advancement) {
        PacketDistributor.sendToPlayer(player, new MentionToastPayload(advancement));
    }

    public static void sendPreferenceUpdate(MentionPreferences preferences) {
        PacketDistributor.sendToServer(new MentionPreferencesUpdatePayload(preferences));
    }

    public static void requestPreferencesSync() {
        PacketDistributor.sendToServer(new MentionPreferencesRequestPayload());
    }

    private static void handleSync(MentionPreferencesSyncPayload payload, @NotNull IPayloadContext context) {
        context.enqueueWork(() -> ClientMentionPreferences.update(payload.preferences()));
    }

    private static void handleUpdate(MentionPreferencesUpdatePayload payload, @NotNull IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                MentionPreferences attachment = player.getData(CallYouAttachments.MENTION_PREFERENCES.get());
                attachment.copyFrom(payload.preferences());
                syncPreferences(player);
            }
        });
    }

    private static void handleRequest(MentionPreferencesRequestPayload payload, @NotNull IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                syncPreferences(player);
            }
        });
    }

    private static void handleToast(MentionToastPayload payload, @NotNull IPayloadContext context) {
        context.enqueueWork(() -> ToastNotifierClient.showToast(payload.advancement()));
    }
}
