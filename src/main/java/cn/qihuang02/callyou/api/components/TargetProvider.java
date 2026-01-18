package cn.qihuang02.callyou.api.components;

import cn.qihuang02.callyou.api.MentionCandidate;
import cn.qihuang02.callyou.api.MentionContext;
import cn.qihuang02.callyou.registry.CallYouRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import org.jetbrains.annotations.NotNull;

public interface TargetProvider extends IDispatchedComponent<TargetProvider, TargetProvider.TargetProviderType> {
    Codec<TargetProvider> CODEC = IDispatchedComponent.codec(CallYouRegistries.TARGET_PROVIDER_TYPES);

    TargetProviderType type();

    void resolveTargets(@NotNull MentionContext context, @NotNull MentionCandidate candidate);

    record TargetProviderType(
            MapCodec<? extends TargetProvider> mapCodec) implements IDispatchedComponent.Type<TargetProvider> {
    }
}
