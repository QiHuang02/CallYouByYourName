package cn.qihuang02.callyou.api.components;

import cn.qihuang02.callyou.api.MentionContext;
import cn.qihuang02.callyou.registry.CallYouRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public interface TargetProvider {
    Codec<TargetProvider> CODEC =
            CallYouRegistries.TARGET_PROVIDER_TYPES
                    .byNameCodec()
                    .dispatch(
                            "type",
                            TargetProvider::type,
                            TargetProviderType::mapCodec
                    );

    TargetProviderType type();

    List<ServerPlayer> getTargets(MentionContext context);

    record TargetProviderType(MapCodec<? extends TargetProvider> mapCodec) {
        @SuppressWarnings("unchecked")
        public Codec<TargetProvider> codec() {
            return (Codec<TargetProvider>) mapCodec.codec();
        }
    }
}