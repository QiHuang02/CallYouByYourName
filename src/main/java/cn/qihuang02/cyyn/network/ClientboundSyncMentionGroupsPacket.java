package cn.qihuang02.cyyn.network;

import cn.qihuang02.cyyn.client.chat.ClientMentionGroupTokens;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public record ClientboundSyncMentionGroupsPacket(@NotNull List<String> tokens) {
    public ClientboundSyncMentionGroupsPacket(@NotNull Collection<String> tokens) {
        this(new ArrayList<>(Objects.requireNonNull(tokens, "tokens")));
    }

    public static void encode(ClientboundSyncMentionGroupsPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.tokens.size());
        for (String token : packet.tokens) {
            buffer.writeUtf(token);
        }
    }

    public static ClientboundSyncMentionGroupsPacket decode(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        List<String> tokens = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            tokens.add(buffer.readUtf(32767));
        }
        return new ClientboundSyncMentionGroupsPacket(tokens);
    }

    public static void handle(ClientboundSyncMentionGroupsPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> new DistExecutor.SafeRunnable() {
            @Override
            public void run() {
                ClientMentionGroupTokens.updateTokens(packet.tokens);
            }
        }));
        context.setPacketHandled(true);
    }
}
