package cn.qihuang02.callyou.core.mention.components.formatter;

import cn.qihuang02.callyou.api.MentionCandidate;
import cn.qihuang02.callyou.api.MentionContext;
import cn.qihuang02.callyou.api.components.InteractionDecorator;
import cn.qihuang02.callyou.api.components.NoopInteractionDecorator;
import cn.qihuang02.callyou.api.components.TextFormatter;
import cn.qihuang02.callyou.registry.BuiltInCallYouRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record SimpleTextFormatter(String formatter, InteractionDecorator decorator) implements TextFormatter {
    public static final MapCodec<SimpleTextFormatter> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("formatter").forGetter(SimpleTextFormatter::formatter),
            InteractionDecorator.CODEC.optionalFieldOf("decorator")
                    .forGetter(formatter -> Optional.ofNullable(formatter.decorator))
    ).apply(instance, (formatter, decoratorOpt) -> new SimpleTextFormatter(formatter, decoratorOpt.orElse(null))));

    public SimpleTextFormatter(String formatter, InteractionDecorator decorator) {
        this.formatter = formatter;
        this.decorator = decorator != null ? decorator : NoopInteractionDecorator.INSTANCE;
    }

    @Contract(pure = true)
    @Override
    public @NotNull TextFormatterType type() {
        return BuiltInCallYouRegistries.SIMPLE_FORMATTER_TYPE;
    }

    @Contract(pure = true)
    @Override
    public @NotNull Component format(@NotNull MentionContext context, @NotNull MentionCandidate candidate) {
        return Component.literal(this.formatter);
    }

    @Override
    public @NotNull InteractionDecorator decorator() {
        return this.decorator;
    }
}
