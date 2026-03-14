package cn.qihuang02.callyou.core.network.packet;

import cn.qihuang02.callyou.config.CallYouConfig;
import cn.qihuang02.callyou.core.network.NetworkHandler;
import cn.qihuang02.callyou.core.saveddata.MentionSavedData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class MentionLogActionPacket {
    private final MentionLogAction action;
    private final UUID targetHistoryId;

    public MentionLogActionPacket(@NotNull MentionLogAction action, @NotNull UUID targetHistoryId) {
        this.action = action;
        this.targetHistoryId = targetHistoryId;
    }

    public MentionLogAction action() {
        return action;
    }

    public UUID targetHistoryId() {
        return targetHistoryId;
    }

    public void encode(@NotNull FriendlyByteBuf buf) {
        buf.writeVarInt(action.ordinal());
        buf.writeUUID(targetHistoryId);
    }

    public static @NotNull MentionLogActionPacket decode(@NotNull FriendlyByteBuf buf) {
        MentionLogAction action = MentionLogAction.values()[buf.readVarInt()];
        UUID targetHistoryId = buf.readUUID();
        return new MentionLogActionPacket(action, targetHistoryId);
    }

    public static void handle(@NotNull MentionLogActionPacket packet, @NotNull Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            if (!CallYouConfig.COMMON.enableServerSideHistory.get()) {
                NetworkHandler.sendLogResponse(player, List.of());
                return;
            }
            MentionSavedData data = MentionSavedData.get(player.serverLevel());
            if (packet.action == MentionLogAction.MARK_ALL_READ) {
                data.markAsRead(player.getUUID());
            } else if (packet.action == MentionLogAction.DELETE_SINGLE) {
                data.removeRecord(player.getUUID(), packet.targetHistoryId);
            } else if (packet.action == MentionLogAction.MARK_SINGLE_READ) {
                data.markAsRead(player.getUUID(), packet.targetHistoryId);
            }
            data.pruneOldLogs();
            NetworkHandler.sendLogResponse(player, data.getLogs(player.getUUID()));
        });
        ctx.get().setPacketHandled(true);
    }

    public enum MentionLogAction {
        MARK_ALL_READ, DELETE_SINGLE, MARK_SINGLE_READ
    }
}
