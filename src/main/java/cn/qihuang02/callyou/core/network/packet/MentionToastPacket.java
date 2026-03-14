package cn.qihuang02.callyou.core.network.packet;

import cn.qihuang02.callyou.core.client.ToastNotifierClient;
import net.minecraft.advancements.Advancement;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class MentionToastPacket {
    private final Advancement advancement;

    public MentionToastPacket(@NotNull Advancement advancement) {
        this.advancement = advancement;
    }

    public Advancement advancement() {
        return advancement;
    }

    public void encode(@NotNull FriendlyByteBuf buf) {
        advancement.deconstruct().serializeToNetwork(buf);
    }

    public static @NotNull MentionToastPacket decode(@NotNull FriendlyByteBuf buf) {
        Advancement.Builder builder = Advancement.Builder.fromNetwork(buf);
        // We need a dummy ResourceLocation for the advancement; the toast only uses display info
        net.minecraft.resources.ResourceLocation dummyId = new net.minecraft.resources.ResourceLocation("callyou", "toast_dummy");
        Advancement advancement = builder.build(dummyId);
        return new MentionToastPacket(advancement);
    }

    public static void handle(@NotNull MentionToastPacket packet, @NotNull Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ToastNotifierClient.showToast(packet.advancement));
        ctx.get().setPacketHandled(true);
    }
}
