package cn.qihuang02.callyou.api;

import cn.qihuang02.callyou.registry.CallYouRegistries;
import com.mojang.serialization.Codec;
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
}