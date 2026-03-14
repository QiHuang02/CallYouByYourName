package cn.qihuang02.callyou.core.network;

import cn.qihuang02.callyou.core.attachment.CallYouAttachments;
import cn.qihuang02.callyou.core.attachment.MentionPreferences;
import cn.qihuang02.callyou.core.network.packet.*;
import net.minecraft.advancements.Advancement;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

public final class NetworkHandler {

    private NetworkHandler() {}

    public static void syncPreferences(@NotNull ServerPlayer player) {
        MentionPreferences preferences = CallYouAttachments.getPreferences(player);
        CallYouNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new MentionPrefsSyncPacket(preferences)
        );
    }

    public static void sendToast(@NotNull ServerPlayer player, @NotNull Advancement advancement) {
        CallYouNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new MentionToastPacket(advancement)
        );
    }

    public static void sendPreferenceUpdate(MentionPreferences preferences) {
        CallYouNetwork.CHANNEL.sendToServer(new MentionPrefsUpdatePacket(preferences));
    }

    public static void requestPreferencesSync() {
        CallYouNetwork.CHANNEL.sendToServer(new MentionPrefsRequestPacket());
    }

    public static void requestMentionLogs() {
        cn.qihuang02.callyou.core.client.ClientMentionHistory.markAwaitingResponse();
        CallYouNetwork.CHANNEL.sendToServer(new MentionLogRequestPacket());
    }

    public static void sendMentionLogAction(
            @NotNull MentionLogActionPacket.MentionLogAction action,
            @NotNull java.util.UUID targetHistoryId
    ) {
        cn.qihuang02.callyou.core.client.ClientMentionHistory.markAwaitingResponse();
        CallYouNetwork.CHANNEL.sendToServer(new MentionLogActionPacket(action, targetHistoryId));
    }

    public static void sendLogResponse(@NotNull ServerPlayer player, @NotNull java.util.List<cn.qihuang02.callyou.core.saveddata.MentionRecord> records) {
        CallYouNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new MentionLogResponsePacket(records)
        );
    }
}
