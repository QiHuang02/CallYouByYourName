package cn.qihuang02.cyyn.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientboundPlayAtSoundPacket {
    public ClientboundPlayAtSoundPacket() {}

    public static ClientboundPlayAtSoundPacket decode(FriendlyByteBuf buf) {
        return new ClientboundPlayAtSoundPacket();
    }

    public static void encode(ClientboundPlayAtSoundPacket packet, FriendlyByteBuf buf) {}

    public static boolean handle(ClientboundPlayAtSoundPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() -> {
            Minecraft.getInstance().getSoundManager().play(
                    SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_BELL.get(), 1.0F, 1.0F)
            );
        });

        return true;
    }
}
