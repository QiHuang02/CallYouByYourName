package cn.qihuang02.cyyn.network;

import cn.qihuang02.cyyn.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public record ClientboundPlayAtSoundPacket(ResourceLocation soundLocation) {

    public static @NotNull ClientboundPlayAtSoundPacket decode(@NotNull FriendlyByteBuf buf) {
        ResourceLocation soundLocation = buf.readResourceLocation();
        return new ClientboundPlayAtSoundPacket(soundLocation);
    }

    public static void encode(@NotNull ClientboundPlayAtSoundPacket packet, @NotNull FriendlyByteBuf buf) {
        buf.writeResourceLocation(packet.soundLocation());
    }

    public static boolean handle(ClientboundPlayAtSoundPacket packet, @NotNull Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() -> {
            if (!Config.enableMentionSound) {
                return;
            }

            SoundEvent soundEvent = ForgeRegistries.SOUND_EVENTS.getValue(packet.soundLocation());
            if (soundEvent != null) {
                Minecraft.getInstance().getSoundManager().play(
                        SimpleSoundInstance.forUI(soundEvent, 1.0F, 1.0F)
                );
            }
        });

        context.setPacketHandled(true);
        return true;
    }
}
