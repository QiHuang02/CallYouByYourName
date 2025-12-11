package cn.qihuang02.callyou.network.payload;

import cn.qihuang02.callyou.CallYouByYourName;
import cn.qihuang02.callyou.attachment.MentionPreferences;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record MentionPreferencesUpdatePayload(MentionPreferences preferences) implements CustomPacketPayload {
    public static final Type<MentionPreferencesUpdatePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(CallYouByYourName.MODID, "mention_prefs_update"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MentionPreferencesUpdatePayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.fromCodec(MentionPreferences.CODEC),
                    MentionPreferencesUpdatePayload::preferences,
                    MentionPreferencesUpdatePayload::new
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
