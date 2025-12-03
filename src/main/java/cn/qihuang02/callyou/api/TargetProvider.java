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

    /**
     * The type info used for serialization and registry dispatch.
     */
    TargetProviderType type();


    /**
     * Return the list of target players for this mention.
     */
    List<ServerPlayer> getTargets(MentionContext context);
}