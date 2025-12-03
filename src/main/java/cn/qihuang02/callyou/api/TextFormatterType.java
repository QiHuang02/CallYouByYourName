package cn.qihuang02.callyou.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

public record TextFormatterType(MapCodec<? extends TextFormatter> mapCodec) {
    @SuppressWarnings("unchecked")
    public Codec<TextFormatter> codec() {
        return (Codec<TextFormatter>) mapCodec.codec();
    }
}
