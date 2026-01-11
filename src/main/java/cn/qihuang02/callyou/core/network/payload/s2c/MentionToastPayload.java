package cn.qihuang02.callyou.core.network.payload.s2c;

import cn.qihuang02.callyou.CallYouByYourName;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record MentionToastPayload(AdvancementHolder advancement) implements CustomPacketPayload {
    public static final Type<MentionToastPayload> TYPE = new Type<>(CallYouByYourName.getRl("mention_toast"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MentionToastPayload> STREAM_CODEC = StreamCodec.composite(AdvancementHolder.STREAM_CODEC, MentionToastPayload::advancement, MentionToastPayload::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}