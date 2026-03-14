package cn.qihuang02.callyou.core.network.packet;

import cn.qihuang02.callyou.api.ResolveStatus;
import cn.qihuang02.callyou.core.client.ClientMentionHistory;
import cn.qihuang02.callyou.core.saveddata.MentionRecord;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class MentionLogResponsePacket {
    private final List<MentionRecord> records;

    public MentionLogResponsePacket(@NotNull List<MentionRecord> records) {
        this.records = records;
    }

    public List<MentionRecord> records() {
        return records;
    }

    public void encode(@NotNull FriendlyByteBuf buf) {
        buf.writeVarInt(records.size());
        for (MentionRecord record : records) {
            buf.writeUUID(record.historyId());
            buf.writeUUID(record.senderId());
            buf.writeUtf(record.senderName());
            buf.writeComponent(record.message());
            buf.writeLong(record.timestamp());
            buf.writeUUID(record.targetId());
            buf.writeBoolean(record.read());
            buf.writeUtf(record.status().name());
            buf.writeUtf(record.originalKey());
        }
    }

    public static @NotNull MentionLogResponsePacket decode(@NotNull FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<MentionRecord> records = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            UUID historyId = buf.readUUID();
            UUID senderId = buf.readUUID();
            String senderName = buf.readUtf();
            Component message = buf.readComponent();
            long timestamp = buf.readLong();
            UUID targetId = buf.readUUID();
            boolean read = buf.readBoolean();
            ResolveStatus status = ResolveStatus.valueOf(buf.readUtf());
            String originalKey = buf.readUtf();
            records.add(new MentionRecord(historyId, senderId, senderName, message, timestamp, targetId, read, status, originalKey));
        }
        return new MentionLogResponsePacket(records);
    }

    public static void handle(@NotNull MentionLogResponsePacket packet, @NotNull Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientMentionHistory.update(packet.records));
        ctx.get().setPacketHandled(true);
    }
}
