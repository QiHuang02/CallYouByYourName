package cn.qihuang02.callyou.core.network.packet;

import cn.qihuang02.callyou.config.CallYouConfig;
import cn.qihuang02.callyou.core.network.NetworkHandler;
import cn.qihuang02.callyou.core.saveddata.MentionSavedData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

public class MentionLogRequestPacket {

    public MentionLogRequestPacket() {
    }

    public void encode(@NotNull FriendlyByteBuf buf) {
        // No data
    }

    public static @NotNull MentionLogRequestPacket decode(@NotNull FriendlyByteBuf buf) {
        return new MentionLogRequestPacket();
    }

    public static void handle(@NotNull MentionLogRequestPacket packet, @NotNull Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            if (!CallYouConfig.COMMON.enableServerSideHistory.get()) {
                NetworkHandler.sendLogResponse(player, List.of());
                return;
            }
            MentionSavedData data = MentionSavedData.get(player.serverLevel());
            data.pruneOldLogs();
            NetworkHandler.sendLogResponse(player, data.getLogs(player.getUUID()));
        });
        ctx.get().setPacketHandled(true);
    }
}
