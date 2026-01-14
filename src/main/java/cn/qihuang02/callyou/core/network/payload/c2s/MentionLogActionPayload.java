package cn.qihuang02.callyou.core.network.payload.c2s;

import cn.qihuang02.callyou.CallYouByYourName;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record MentionLogActionPayload(MentionLogAction action, UUID targetHistoryId) implements CustomPacketPayload {
    public static final Type<MentionLogActionPayload> TYPE = new Type<>(CallYouByYourName.getRl("mention_log_action"));
    private static final StreamCodec<RegistryFriendlyByteBuf, UUID> UUID_STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_LONG,
                    UUID::getMostSignificantBits,
                    ByteBufCodecs.VAR_LONG,
                    UUID::getLeastSignificantBits,
                    UUID::new
            );
    public static final StreamCodec<RegistryFriendlyByteBuf, MentionLogActionPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.idMapper(id -> MentionLogAction.values()[id], MentionLogAction::ordinal),
                    MentionLogActionPayload::action,
                    UUID_STREAM_CODEC,
                    MentionLogActionPayload::targetHistoryId,
                    MentionLogActionPayload::new
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public enum MentionLogAction {
        MARK_ALL_READ, DELETE_SINGLE, MARK_SINGLE_READ
    }
}
