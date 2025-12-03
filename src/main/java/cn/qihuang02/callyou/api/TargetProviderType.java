package cn.qihuang02.callyou.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

public record TargetProviderType(MapCodec<? extends TargetProvider> mapCodec) {
    @SuppressWarnings("unchecked")
    public Codec<TargetProvider> codec() {
        return (Codec<TargetProvider>) mapCodec.codec();
    }
}
