package cn.qihuang02.callyou.network;

import cn.qihuang02.callyou.CallYouByYourName;
import cn.qihuang02.callyou.client.ClientMentionHistory;
import cn.qihuang02.callyou.client.ClientMentionPreferences;
import cn.qihuang02.callyou.client.ToastNotifierClient;
import cn.qihuang02.callyou.config.CallYouConfig;
import cn.qihuang02.callyou.core.attachment.CallYouAttachments;
import cn.qihuang02.callyou.core.attachment.MentionPreferences;
import cn.qihuang02.callyou.core.saveddata.MentionRecord;
import cn.qihuang02.callyou.core.saveddata.MentionSavedData;
import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacket;
import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacketDistributor;
import com.lowdragmc.lowdraglib2.syncdata.rpc.RPCSender;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CYRPCPacket {
    private static final String RPC_PREFS_SYNC = "mention_prefs_sync";
    private static final String RPC_PREFS_UPDATE = "mention_prefs_update";
    private static final String RPC_PREFS_REQUEST = "mention_prefs_request";
    private static final String RPC_TOAST = "mention_toast";
    private static final String RPC_LOG_REQUEST = "mention_log_request";
    private static final String RPC_LOG_RESPONSE = "mention_log_response";
    private static final String RPC_LOG_ACTION = "mention_log_action";

    @RPCPacket(value = RPC_PREFS_SYNC, modId = CallYouByYourName.MODID)
    public static void handleSync(MentionPreferences preferences) {
        ClientMentionPreferences.update(preferences);
    }

    @RPCPacket(value = RPC_PREFS_UPDATE, modId = CallYouByYourName.MODID)
    public static void handleUpdate(@NotNull RPCSender sender, MentionPreferences preferences) {
        ServerPlayer player = sender.asPlayer();
        if (player == null) {
            return;
        }
        MentionPreferences attachment = player.getData(CallYouAttachments.MENTION_PREFERENCES.get());
        attachment.copyFrom(preferences);
        syncPreferences(player);
    }

    @RPCPacket(value = RPC_PREFS_REQUEST, modId = CallYouByYourName.MODID)
    public static void handleRequest(@NotNull RPCSender sender) {
        ServerPlayer player = sender.asPlayer();
        if (player != null) {
            syncPreferences(player);
        }
    }

    @RPCPacket(value = RPC_TOAST, modId = CallYouByYourName.MODID)
    public static void handleToast(AdvancementHolder advancement) {
        ToastNotifierClient.showToast(advancement);
    }

    @RPCPacket(value = RPC_LOG_REQUEST, modId = CallYouByYourName.MODID)
    public static void handleLogRequest(@NotNull RPCSender sender) {
        ServerPlayer player = sender.asPlayer();
        if (player == null) {
            return;
        }
        if (!CallYouConfig.COMMON.enableServerSideHistory.get()) {
            RPCPacketDistributor.rpcToPlayer(player, RPC_LOG_RESPONSE, List.of());
            return;
        }
        MentionSavedData data = MentionSavedData.get(player.serverLevel());
        data.pruneOldLogs();
        sendLogSnapshot(player, data);
    }

    @RPCPacket(value = RPC_LOG_RESPONSE, modId = CallYouByYourName.MODID)
    public static void handleLogResponse(List<MentionRecord> records) {
        ClientMentionHistory.update(records);
    }

    @RPCPacket(value = RPC_LOG_ACTION, modId = CallYouByYourName.MODID)
    public static void handleLogAction(@NotNull RPCSender sender, CallYouNetwork.MentionLogAction action, long targetTimestamp) {
        ServerPlayer player = sender.asPlayer();
        if (player == null) {
            return;
        }
        if (!CallYouConfig.COMMON.enableServerSideHistory.get()) {
            RPCPacketDistributor.rpcToPlayer(player, RPC_LOG_RESPONSE, List.of());
            return;
        }
        MentionSavedData data = MentionSavedData.get(player.serverLevel());
        if (action == CallYouNetwork.MentionLogAction.MARK_ALL_READ) {
            data.markAsRead(player.getUUID());
        } else if (action == CallYouNetwork.MentionLogAction.DELETE_SINGLE) {
            data.removeRecord(player.getUUID(), targetTimestamp);
        }
        data.pruneOldLogs();
        sendLogSnapshot(player, data);
    }

    public static void syncPreferences(@NotNull ServerPlayer player) {
        MentionPreferences preferences = player.getData(CallYouAttachments.MENTION_PREFERENCES.get());
        RPCPacketDistributor.rpcToPlayer(player, RPC_PREFS_SYNC, preferences);
    }

    public static void sendToast(@NotNull ServerPlayer player, @NotNull AdvancementHolder advancement) {
        RPCPacketDistributor.rpcToPlayer(player, RPC_TOAST, advancement);
    }

    public static void sendPreferenceUpdate(MentionPreferences preferences) {
        RPCPacketDistributor.rpcToServer(RPC_PREFS_UPDATE, preferences);
    }

    public static void requestPreferencesSync() {
        RPCPacketDistributor.rpcToServer(RPC_PREFS_REQUEST);
    }

    public static void requestMentionLogs() {
        ClientMentionHistory.markAwaitingResponse();
        RPCPacketDistributor.rpcToServer(RPC_LOG_REQUEST);
    }

    public static void sendMentionLogAction(@NotNull CallYouNetwork.MentionLogAction action, long targetTimestamp) {
        ClientMentionHistory.markAwaitingResponse();
        RPCPacketDistributor.rpcToServer(RPC_LOG_ACTION, action, targetTimestamp);
    }

    private static void sendLogSnapshot(@NotNull ServerPlayer player, @NotNull MentionSavedData data) {
        RPCPacketDistributor.rpcToPlayer(player, RPC_LOG_RESPONSE, data.getLogs(player.getUUID()));
    }
}
