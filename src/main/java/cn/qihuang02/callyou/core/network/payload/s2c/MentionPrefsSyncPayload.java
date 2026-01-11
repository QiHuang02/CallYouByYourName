package cn.qihuang02.callyou.core.network.payload.s2c;

import cn.qihuang02.callyou.CallYouByYourName;
import cn.qihuang02.callyou.core.attachment.MentionPreferences;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record MentionPrefsSyncPayload(MentionPreferences preferences) implements CustomPacketPayload {
    public static final Type<MentionPrefsSyncPayload> TYPE = new Type<>(CallYouByYourName.getRl("mention_prefs_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MentionPrefsSyncPayload> STREAM_CODEC = StreamCodec.composite(MentionPreferences.STREAM_CODEC, MentionPrefsSyncPayload::preferences, MentionPrefsSyncPayload::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}