package cn.qihuang02.callyou.network.payload;

import cn.qihuang02.callyou.CallYouByYourName;
import cn.qihuang02.callyou.core.storage.MentionRecord;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record MentionLogResponsePayload(@NotNull List<MentionRecord> records) implements CustomPacketPayload {
    public static final Type<MentionLogResponsePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(CallYouByYourName.MODID, "mention_log_response"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MentionLogResponsePayload> STREAM_CODEC =
            StreamCodec.of(MentionLogResponsePayload::encode, MentionLogResponsePayload::decode);

    private static void encode(@NotNull RegistryFriendlyByteBuf buf, @NotNull MentionLogResponsePayload payload) {
        List<MentionRecord> records = payload.records;
        buf.writeVarInt(records.size());
        for (MentionRecord record : records) {
            MentionRecord.STREAM_CODEC.encode(buf, record);
        }
    }

    @Contract("_ -> new")
    private static @NotNull MentionLogResponsePayload decode(@NotNull RegistryFriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<MentionRecord> list = new ArrayList<>(Math.max(size, 0));
        for (int i = 0; i < size; i++) {
            list.add(MentionRecord.STREAM_CODEC.decode(buf));
        }
        return new MentionLogResponsePayload(list);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
