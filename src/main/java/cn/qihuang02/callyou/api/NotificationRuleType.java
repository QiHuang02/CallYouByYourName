package cn.qihuang02.callyou.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

public record NotificationRuleType(MapCodec<? extends NotificationRule> mapCodec) {
    @SuppressWarnings("unchecked")
    public Codec<NotificationRule> codec() {
        return (Codec<NotificationRule>) mapCodec.codec();
    }
}
