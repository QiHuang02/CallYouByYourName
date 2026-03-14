package cn.qihuang02.callyou.core.network.packet;

import cn.qihuang02.callyou.core.attachment.CallYouAttachments;
import cn.qihuang02.callyou.core.attachment.MentionPreferences;
import cn.qihuang02.callyou.core.network.NetworkHandler;
import cn.qihuang02.callyou.core.saveddata.MentionPreferencesSavedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class MentionPrefsUpdatePacket {
    private final MentionPreferences preferences;

    public MentionPrefsUpdatePacket(@NotNull MentionPreferences preferences) {
        this.preferences = preferences;
    }

    public MentionPreferences preferences() {
        return preferences;
    }

    public void encode(@NotNull FriendlyByteBuf buf) {
        CompoundTag tag = (CompoundTag) MentionPreferences.CODEC.encodeStart(NbtOps.INSTANCE, preferences)
                .getOrThrow(false, s -> {});
        buf.writeNbt(tag);
    }

    public static @NotNull MentionPrefsUpdatePacket decode(@NotNull FriendlyByteBuf buf) {
        CompoundTag tag = buf.readNbt();
        MentionPreferences prefs = MentionPreferences.CODEC.parse(NbtOps.INSTANCE, tag)
                .getOrThrow(false, s -> {});
        return new MentionPrefsUpdatePacket(prefs);
    }

    public static void handle(@NotNull MentionPrefsUpdatePacket packet, @NotNull Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            MentionPreferences attachment = CallYouAttachments.getPreferences(player);
            attachment.copyFrom(packet.preferences);
            CallYouAttachments.setPreferences(player, attachment);
            NetworkHandler.syncPreferences(player);
        });
        ctx.get().setPacketHandled(true);
    }
}
