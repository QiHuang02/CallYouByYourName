package cn.qihuang02.callyou.core.network.payload.c2s;

import cn.qihuang02.callyou.CallYouByYourName;
import cn.qihuang02.callyou.core.attachment.MentionPreferences;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record MentionPrefsUpdatePayload(MentionPreferences preferences) implements CustomPacketPayload {
    public static final Type<MentionPrefsUpdatePayload> TYPE = new Type<>(CallYouByYourName.getRl("mention_prefs_update"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MentionPrefsUpdatePayload> STREAM_CODEC = StreamCodec.composite(MentionPreferences.STREAM_CODEC, MentionPrefsUpdatePayload::preferences, MentionPrefsUpdatePayload::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}