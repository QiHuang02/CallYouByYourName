package cn.qihuang02.cyyn.common.network.packet;

import cn.qihuang02.cyyn.client.chat.ClientMentionGroupNames;
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

public record SyncMentionGroupsPacket(@NotNull List<String> names) {
    public SyncMentionGroupsPacket(@NotNull Collection<String> names) {
        this(new ArrayList<>(Objects.requireNonNull(names, "names")));
    }

    public static void encode(SyncMentionGroupsPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.names.size());
        for (String name : packet.names) {
            buffer.writeUtf(name);
        }
    }

    public static SyncMentionGroupsPacket decode(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        List<String> names = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            names.add(buffer.readUtf(32767));
        }
        return new SyncMentionGroupsPacket(names);
    }

    public static void handle(SyncMentionGroupsPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> new DistExecutor.SafeRunnable() {
            @Override
            public void run() {
                ClientMentionGroupNames.updateNames(packet.names);
            }
        }));
        context.setPacketHandled(true);
    }
}
