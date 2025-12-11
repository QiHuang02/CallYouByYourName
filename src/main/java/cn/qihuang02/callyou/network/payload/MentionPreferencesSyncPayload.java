package cn.qihuang02.callyou.network.payload;

import cn.qihuang02.callyou.CallYouByYourName;
import cn.qihuang02.callyou.attachment.MentionPreferences;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record MentionPreferencesSyncPayload(MentionPreferences preferences) implements CustomPacketPayload {

    public static final Type<MentionPreferencesSyncPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(CallYouByYourName.MODID, "mention_prefs_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MentionPreferencesSyncPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.fromCodec(MentionPreferences.CODEC),
                    MentionPreferencesSyncPayload::preferences,
                    MentionPreferencesSyncPayload::new
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
