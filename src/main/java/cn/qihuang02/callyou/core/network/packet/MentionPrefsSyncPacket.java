package cn.qihuang02.callyou.core.network.packet;

import cn.qihuang02.callyou.core.attachment.MentionPreferences;
import cn.qihuang02.callyou.core.client.ClientMentionPreferences;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class MentionPrefsSyncPacket {
    private final MentionPreferences preferences;

    public MentionPrefsSyncPacket(@NotNull MentionPreferences preferences) {
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

    public static @NotNull MentionPrefsSyncPacket decode(@NotNull FriendlyByteBuf buf) {
        CompoundTag tag = buf.readNbt();
        MentionPreferences prefs = MentionPreferences.CODEC.parse(NbtOps.INSTANCE, tag)
                .getOrThrow(false, s -> {});
        return new MentionPrefsSyncPacket(prefs);
    }

    public static void handle(@NotNull MentionPrefsSyncPacket packet, @NotNull Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientMentionPreferences.update(packet.preferences));
        ctx.get().setPacketHandled(true);
    }
}
