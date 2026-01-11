package cn.qihuang02.callyou.core.network.payload.c2s;

import cn.qihuang02.callyou.CallYouByYourName;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record MentionLogActionPayload(MentionLogAction action, long targetTimestamp) implements CustomPacketPayload {
    public static final Type<MentionLogActionPayload> TYPE = new Type<>(CallYouByYourName.getRl("mention_log_action"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MentionLogActionPayload> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.idMapper(id -> MentionLogAction.values()[id], MentionLogAction::ordinal), MentionLogActionPayload::action, ByteBufCodecs.VAR_LONG, MentionLogActionPayload::targetTimestamp, MentionLogActionPayload::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public enum MentionLogAction {
        MARK_ALL_READ, DELETE_SINGLE
    }
}