package cn.qihuang02.callyou.api;

import cn.qihuang02.callyou.registry.CallYouRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public interface Notifier {
    Codec<Notifier> CODEC =
            CallYouRegistries.NOTIFICATION_RULE_TYPES
                    .byNameCodec()
                    .dispatch(
                            "type",
                            Notifier::type,
                            NotifierType::mapCodec
                    );

    NotifierType type();

    void apply(MentionContext context, List<ServerPlayer> targets);

    record NotifierType(MapCodec<? extends Notifier> mapCodec) {
        @SuppressWarnings("unchecked")
        public Codec<Notifier> codec() {
            return (Codec<Notifier>) mapCodec.codec();
        }
    }
}