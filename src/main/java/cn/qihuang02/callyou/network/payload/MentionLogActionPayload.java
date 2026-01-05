package cn.qihuang02.callyou.network.payload;

import cn.qihuang02.callyou.CallYouByYourName;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record MentionLogActionPayload(@NotNull Action action, long targetTimestamp) implements CustomPacketPayload {
    public enum Action {
        MARK_ALL_READ,
        DELETE_SINGLE
    }

    public static final Type<MentionLogActionPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(CallYouByYourName.MODID, "mention_log_action"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MentionLogActionPayload> STREAM_CODEC =
            StreamCodec.of(MentionLogActionPayload::encode, MentionLogActionPayload::decode);

    private static void encode(@NotNull RegistryFriendlyByteBuf buf, @NotNull MentionLogActionPayload payload) {
        buf.writeEnum(payload.action);
        buf.writeLong(payload.targetTimestamp);
    }

    private static @NotNull MentionLogActionPayload decode(@NotNull RegistryFriendlyByteBuf buf) {
        Action action = buf.readEnum(Action.class);
        long timestamp = buf.readLong();
        return new MentionLogActionPayload(action, timestamp);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
