package cn.qihuang02.callyou.core.network;

import cn.qihuang02.callyou.config.CallYouConfig;
import cn.qihuang02.callyou.core.attachment.CallYouAttachments;
import cn.qihuang02.callyou.core.attachment.MentionPreferences;
import cn.qihuang02.callyou.core.client.ClientMentionHistory;
import cn.qihuang02.callyou.core.client.ClientMentionPreferences;
import cn.qihuang02.callyou.core.client.ToastNotifierClient;
import cn.qihuang02.callyou.core.network.payload.c2s.MentionLogActionPayload;
import cn.qihuang02.callyou.core.network.payload.c2s.MentionLogRequestPayload;
import cn.qihuang02.callyou.core.network.payload.c2s.MentionPrefsRequestPayload;
import cn.qihuang02.callyou.core.network.payload.c2s.MentionPrefsUpdatePayload;
import cn.qihuang02.callyou.core.network.payload.s2c.MentionLogResponsePayload;
import cn.qihuang02.callyou.core.network.payload.s2c.MentionPrefsSyncPayload;
import cn.qihuang02.callyou.core.network.payload.s2c.MentionToastPayload;
import cn.qihuang02.callyou.core.saveddata.MentionPreferencesSavedData;
import cn.qihuang02.callyou.core.saveddata.MentionSavedData;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class NetworkHandler {
    public static void handleSync(MentionPrefsSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientMentionPreferences.update(payload.preferences()));
    }

    public static void handleUpdate(MentionPrefsUpdatePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            MentionPreferences attachment = player.getData(CallYouAttachments.MENTION_PREFERENCES.get());
            attachment.copyFrom(payload.preferences());
            MentionPreferencesSavedData.get(player.serverLevel())
                    .update(player.getUUID(), attachment);
            syncPreferences(player);
        });
    }

    public static void handleRequest(MentionPrefsRequestPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                syncPreferences(player);
            }
        });
    }

    public static void handleToast(MentionToastPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ToastNotifierClient.showToast(payload.advancement()));
    }

    public static void handleLogRequest(MentionLogRequestPayload payload, IPayloadContext context) {
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

    public static void handleLogResponse(MentionLogResponsePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientMentionHistory.update(payload.records()));
    }

    public static void handleLogAction(MentionLogActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            if (!CallYouConfig.COMMON.enableServerSideHistory.get()) {
                PacketDistributor.sendToPlayer(player, new MentionLogResponsePayload(List.of()));
                return;
            }
            MentionSavedData data = MentionSavedData.get(player.serverLevel());
            if (payload.action() == MentionLogActionPayload.MentionLogAction.MARK_ALL_READ) {
                data.markAsRead(player.getUUID());
            } else if (payload.action() == MentionLogActionPayload.MentionLogAction.DELETE_SINGLE) {
                data.removeRecord(player.getUUID(), payload.targetHistoryId());
            } else if (payload.action() == MentionLogActionPayload.MentionLogAction.MARK_SINGLE_READ) {
                data.markAsRead(player.getUUID(), payload.targetHistoryId());
            }
            data.pruneOldLogs();
            sendLogSnapshot(player, data);
        });
    }

    public static void syncPreferences(@NotNull ServerPlayer player) {
        MentionPreferences preferences = player.getData(CallYouAttachments.MENTION_PREFERENCES.get());
        PacketDistributor.sendToPlayer(player, new MentionPrefsSyncPayload(preferences));
    }

    public static void sendToast(@NotNull ServerPlayer player, @NotNull AdvancementHolder advancement) {
        PacketDistributor.sendToPlayer(player, new MentionToastPayload(advancement));
    }

    public static void sendPreferenceUpdate(MentionPreferences preferences) {
        PacketDistributor.sendToServer(new MentionPrefsUpdatePayload(preferences));
    }

    public static void requestPreferencesSync() {
        PacketDistributor.sendToServer(new MentionPrefsRequestPayload());
    }

    public static void requestMentionLogs() {
        ClientMentionHistory.markAwaitingResponse();
        PacketDistributor.sendToServer(new MentionLogRequestPayload());
    }

    public static void sendMentionLogAction(
            @NotNull MentionLogActionPayload.MentionLogAction action,
            @NotNull UUID targetHistoryId
    ) {
        ClientMentionHistory.markAwaitingResponse();
        PacketDistributor.sendToServer(new MentionLogActionPayload(action, targetHistoryId));
    }

    private static void sendLogSnapshot(@NotNull ServerPlayer player, @NotNull MentionSavedData data) {
        PacketDistributor.sendToPlayer(player, new MentionLogResponsePayload(data.getLogs(player.getUUID())));
    }
}
