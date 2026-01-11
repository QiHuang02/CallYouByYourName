package cn.qihuang02.callyou.core.network.payload.s2c;

import cn.qihuang02.callyou.CallYouByYourName;
import cn.qihuang02.callyou.core.saveddata.MentionRecord;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record MentionLogResponsePayload(List<MentionRecord> records) implements CustomPacketPayload {
    public static final Type<MentionLogResponsePayload> TYPE = new Type<>(CallYouByYourName.getRl("mention_log_response"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MentionLogResponsePayload> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.collection(java.util.ArrayList::new, MentionRecord.STREAM_CODEC), MentionLogResponsePayload::records, MentionLogResponsePayload::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}