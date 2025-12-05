package cn.qihuang02.callyou.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;

public record MentionRules(int minOpLevel, @Nullable String permission, boolean isMass) {
    public static final MentionRules DEFAULT = new MentionRules(0, null, false);

    public static final Codec<MentionRules> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.INT.optionalFieldOf("min_op_level", 0)
                            .forGetter(MentionRules::minOpLevel),


                    Codec.STRING.optionalFieldOf("permission", "")
                            .forGetter(rules -> rules.permission() == null ? "" : rules.permission()),


                    Codec.BOOL.optionalFieldOf("is_mass", false)
                            .forGetter(MentionRules::isMass)
            ).apply(instance, (minOpLevel, permission, isMass) -> new MentionRules(minOpLevel, permission.isEmpty() ? null : permission, isMass)));
}
