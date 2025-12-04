package cn.qihuang02.callyou.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record MentionType(
        TargetProvider targetProvider,
        TextFormatter textFormatter,
        NotificationRule notificationRule,
        MentionRules rules
) {
    public static final Codec<MentionType> MENTION_TYPE_CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                            TargetProvider.CODEC.fieldOf("target").forGetter(MentionType::targetProvider),
                            TextFormatter.CODEC.fieldOf("format").forGetter(MentionType::textFormatter),
                            NotificationRule.CODEC.fieldOf("notification").forGetter(MentionType::notificationRule),
                            MentionRules.CODEC.optionalFieldOf("rules", MentionRules.DEFAULT).forGetter(MentionType::rules)
                    ).apply(instance, MentionType::new)
            );
}
