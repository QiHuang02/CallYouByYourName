package cn.qihuang02.callyou.core.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public record MentionRecord(
        @NotNull UUID senderId,
        @NotNull String senderName,
        @NotNull Component message,
        long timestamp,
        boolean read,
        @Nullable GlobalPos location
) {
    public static final Codec<MentionRecord> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.xmap(UUID::fromString, UUID::toString)
                    .fieldOf("sender_id")
                    .forGetter(MentionRecord::senderId),
            Codec.STRING
                    .fieldOf("sender_name")
                    .forGetter(MentionRecord::senderName),
            ComponentSerialization.CODEC
                    .fieldOf("message")
                    .forGetter(MentionRecord::message),
            Codec.LONG
                    .fieldOf("timestamp")
                    .forGetter(MentionRecord::timestamp),
            Codec.BOOL.optionalFieldOf("read", false)
                    .forGetter(MentionRecord::read),
            GlobalPos.CODEC.optionalFieldOf("location")
                    .xmap(opt -> opt.orElse(null), Optional::ofNullable)
                    .forGetter(MentionRecord::location)
    ).apply(instance, MentionRecord::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MentionRecord> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public MentionRecord markRead() {
        if (this.read) {
            return this;
        }
        return new MentionRecord(senderId, senderName, message, timestamp, true, location);
    }
}
