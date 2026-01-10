package cn.qihuang02.callyou.api;

import cn.qihuang02.callyou.api.components.MentionRules;
import cn.qihuang02.callyou.api.components.Notifier;
import cn.qihuang02.callyou.api.components.TargetProvider;
import cn.qihuang02.callyou.api.components.TextFormatter;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record MentionType(
        TargetProvider targetProvider,
        TextFormatter textFormatter,
        Notifier notifier,
        MentionRules rules
) {
    public static final Codec<MentionType> MENTION_TYPE_CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                            TargetProvider.CODEC.fieldOf("target").forGetter(MentionType::targetProvider),
                            TextFormatter.CODEC.fieldOf("format").forGetter(MentionType::textFormatter),
                            Notifier.CODEC.fieldOf("notifier").forGetter(MentionType::notifier),
                            MentionRules.CODEC.optionalFieldOf("rules", MentionRules.DEFAULT).forGetter(MentionType::rules)
                    ).apply(instance, MentionType::new)
            );
}
