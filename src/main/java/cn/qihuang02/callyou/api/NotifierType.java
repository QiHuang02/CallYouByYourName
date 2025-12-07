package cn.qihuang02.callyou.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

public record NotifierType(MapCodec<? extends Notifier> mapCodec) {
    @SuppressWarnings("unchecked")
    public Codec<Notifier> codec() {
        return (Codec<Notifier>) mapCodec.codec();
    }
}
