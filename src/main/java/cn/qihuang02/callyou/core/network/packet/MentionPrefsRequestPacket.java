package cn.qihuang02.callyou.core.network.packet;

import cn.qihuang02.callyou.core.network.NetworkHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class MentionPrefsRequestPacket {

    public MentionPrefsRequestPacket() {
    }

    public void encode(@NotNull FriendlyByteBuf buf) {
        // No data
    }

    public static @NotNull MentionPrefsRequestPacket decode(@NotNull FriendlyByteBuf buf) {
        return new MentionPrefsRequestPacket();
    }

    public static void handle(@NotNull MentionPrefsRequestPacket packet, @NotNull Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                NetworkHandler.syncPreferences(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
