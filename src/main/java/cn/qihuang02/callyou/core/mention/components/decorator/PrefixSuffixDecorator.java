package cn.qihuang02.callyou.core.mention.components.decorator;

import cn.qihuang02.callyou.api.MentionCandidate;
import cn.qihuang02.callyou.api.MentionContext;
import cn.qihuang02.callyou.api.MentionView;
import cn.qihuang02.callyou.api.components.InteractionDecorator;
import cn.qihuang02.callyou.registry.BuiltInCallYouRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;

public record PrefixSuffixDecorator(String prefix, String suffix) implements InteractionDecorator {
    public static final MapCodec<PrefixSuffixDecorator> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.optionalFieldOf("prefix", "").forGetter(PrefixSuffixDecorator::prefix),
            Codec.STRING.optionalFieldOf("suffix", "").forGetter(PrefixSuffixDecorator::suffix)
    ).apply(instance, PrefixSuffixDecorator::new));

    @Override
    public @NotNull InteractionDecoratorType type() {
        return BuiltInCallYouRegistries.PREFIX_SUFFIX_DECORATOR_TYPE;
    }

    @Override
    public @NotNull Component decorate(
            @NotNull Component original,
            @NotNull MentionContext context,
            @NotNull MentionCandidate candidate,
            @NotNull MentionView view
    ) {
        if (this.prefix.isEmpty() && this.suffix.isEmpty()) {
            return original;
        }
        MutableComponent wrapped = Component.empty();
        if (!this.prefix.isEmpty()) {
            wrapped.append(this.prefix);
        }
        wrapped.append(original);
        if (!this.suffix.isEmpty()) {
            wrapped.append(this.suffix);
        }
        return wrapped;
    }
}
