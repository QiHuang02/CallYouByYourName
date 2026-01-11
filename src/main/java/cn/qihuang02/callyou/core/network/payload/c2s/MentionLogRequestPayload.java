package cn.qihuang02.callyou.core.network.payload.c2s;

import cn.qihuang02.callyou.CallYouByYourName;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record MentionLogRequestPayload() implements CustomPacketPayload {
    public static final Type<MentionLogRequestPayload> TYPE = new Type<>(CallYouByYourName.getRl("mention_log_request"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MentionLogRequestPayload> STREAM_CODEC = StreamCodec.unit(new MentionLogRequestPayload());

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}