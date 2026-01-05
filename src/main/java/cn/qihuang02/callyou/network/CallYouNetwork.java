package cn.qihuang02.callyou.network;

import cn.qihuang02.callyou.CallYouByYourName;
import cn.qihuang02.callyou.config.CallYouConfig;
import cn.qihuang02.callyou.core.attachment.CallYouAttachments;
import cn.qihuang02.callyou.core.attachment.MentionPreferences;
import cn.qihuang02.callyou.client.ClientMentionHistory;
import cn.qihuang02.callyou.client.ClientMentionPreferences;
import cn.qihuang02.callyou.client.ToastNotifierClient;
import cn.qihuang02.callyou.core.storage.MentionSavedData;
import cn.qihuang02.callyou.network.payload.MentionLogActionPayload;
import cn.qihuang02.callyou.network.payload.MentionLogRequestPayload;
import cn.qihuang02.callyou.network.payload.MentionLogResponsePayload;
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

import java.util.List;

@EventBusSubscriber(modid = CallYouByYourName.MODID)
public final class CallYouNetwork {
    private static final String VERSION = "1.1";

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

        registrar.playToServer(
                MentionLogRequestPayload.TYPE,
                MentionLogRequestPayload.STREAM_CODEC,
                CallYouNetwork::handleLogRequest
        );

        registrar.playToClient(
                MentionLogResponsePayload.TYPE,
                MentionLogResponsePayload.STREAM_CODEC,
                CallYouNetwork::handleLogResponse
        );

        registrar.playToServer(
                MentionLogActionPayload.TYPE,
                MentionLogActionPayload.STREAM_CODEC,
                CallYouNetwork::handleLogAction
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

    public static void requestMentionLogs() {
        ClientMentionHistory.markAwaitingResponse();
        PacketDistributor.sendToServer(new MentionLogRequestPayload());
    }

    public static void sendMentionLogAction(@NotNull MentionLogActionPayload.Action action, long targetTimestamp) {
        ClientMentionHistory.markAwaitingResponse();
        PacketDistributor.sendToServer(new MentionLogActionPayload(action, targetTimestamp));
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

    private static void handleLogRequest(MentionLogRequestPayload payload, @NotNull IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            if (!CallYouConfig.COMMON.enableServerSideHistory.get()) {
                PacketDistributor.sendToPlayer(player, new MentionLogResponsePayload(List.of()));
                return;
            }
            MentionSavedData data = MentionSavedData.get(player.serverLevel());
            data.pruneOldLogs();
            sendLogSnapshot(player, data);
        });
    }

    private static void handleLogResponse(MentionLogResponsePayload payload, @NotNull IPayloadContext context) {
        context.enqueueWork(() -> ClientMentionHistory.update(payload.records()));
    }

    private static void handleLogAction(MentionLogActionPayload payload, @NotNull IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            if (!CallYouConfig.COMMON.enableServerSideHistory.get()) {
                PacketDistributor.sendToPlayer(player, new MentionLogResponsePayload(List.of()));
                return;
            }
            MentionSavedData data = MentionSavedData.get(player.serverLevel());
            if (payload.action() == MentionLogActionPayload.Action.MARK_ALL_READ) {
                data.markAsRead(player.getUUID());
            } else if (payload.action() == MentionLogActionPayload.Action.DELETE_SINGLE) {
                data.removeRecord(player.getUUID(), payload.targetTimestamp());
            }
            data.pruneOldLogs();
            sendLogSnapshot(player, data);
        });
    }

    private static void sendLogSnapshot(@NotNull ServerPlayer player, @NotNull MentionSavedData data) {
        PacketDistributor.sendToPlayer(player, new MentionLogResponsePayload(data.getLogs(player.getUUID())));
    }
}
