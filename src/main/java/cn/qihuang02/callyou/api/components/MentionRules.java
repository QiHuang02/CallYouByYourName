package cn.qihuang02.callyou.api.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record MentionRules(int minOpLevel, boolean isMass, boolean allowOfflineHistory) {
    public static final MentionRules DEFAULT = new MentionRules(0, false, false);

    public static final Codec<MentionRules> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.INT.optionalFieldOf("min_op_level", 0)
                            .forGetter(MentionRules::minOpLevel),
                    Codec.BOOL.optionalFieldOf("is_mass", false)
                            .forGetter(MentionRules::isMass),
                    Codec.BOOL.optionalFieldOf("allow_offline_history", false)
                            .forGetter(MentionRules::allowOfflineHistory)
            ).apply(instance, MentionRules::new));
}
