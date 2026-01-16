package cn.qihuang02.callyou.api.components;

import cn.qihuang02.callyou.api.MentionContext;
import cn.qihuang02.callyou.api.TargetCollection;
import cn.qihuang02.callyou.registry.CallYouRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

public interface TargetProvider extends IDispatchedComponent<TargetProvider, TargetProvider.TargetProviderType> {
    Codec<TargetProvider> CODEC = IDispatchedComponent.codec(CallYouRegistries.TARGET_PROVIDER_TYPES);

    TargetProviderType type();

    TargetCollection resolveTargets(MentionContext context);

    record TargetProviderType(
            MapCodec<? extends TargetProvider> mapCodec) implements IDispatchedComponent.Type<TargetProvider> {
    }
}
