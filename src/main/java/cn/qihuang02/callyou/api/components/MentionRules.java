package cn.qihuang02.callyou.api.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record MentionRules(int minOpLevel, boolean isMass) {
    public static final MentionRules DEFAULT = new MentionRules(0, false);

    public static final Codec<MentionRules> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.INT.optionalFieldOf("min_op_level", 0)
                            .forGetter(MentionRules::minOpLevel),
                    Codec.BOOL.optionalFieldOf("is_mass", false)
                            .forGetter(MentionRules::isMass)
            ).apply(instance, MentionRules::new));
}
