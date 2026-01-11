package cn.qihuang02.callyou.core.network.payload.c2s;

import cn.qihuang02.callyou.CallYouByYourName;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record MentionPrefsRequestPayload() implements CustomPacketPayload {
    public static final Type<MentionPrefsRequestPayload> TYPE =
            new Type<>(CallYouByYourName.getRl("mention_prefs_request"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MentionPrefsRequestPayload> STREAM_CODEC =
            StreamCodec.unit(new MentionPrefsRequestPayload());

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}