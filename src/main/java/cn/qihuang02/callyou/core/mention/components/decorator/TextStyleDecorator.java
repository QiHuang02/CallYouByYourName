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
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record TextStyleDecorator(Boolean bold, Boolean italic) implements InteractionDecorator {
    public static final MapCodec<TextStyleDecorator> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("bold").forGetter(style -> Optional.ofNullable(style.bold)),
            Codec.BOOL.optionalFieldOf("italic").forGetter(style -> Optional.ofNullable(style.italic))
    ).apply(instance, (boldOpt, italicOpt) -> new TextStyleDecorator(
            boldOpt.orElse(null),
            italicOpt.orElse(null)
    )));

    private static Style applyStyle(@NotNull Style style, Boolean bold, Boolean italic) {
        Style updated = style;
        if (bold != null) {
            updated = updated.withBold(bold);
        }
        if (italic != null) {
            updated = updated.withItalic(italic);
        }
        return updated;
    }

    @Override
    public @NotNull InteractionDecoratorType type() {
        return BuiltInCallYouRegistries.TEXT_STYLE_DECORATOR_TYPE;
    }

    @Override
    public @NotNull Component decorate(
            @NotNull Component original,
            @NotNull MentionContext context,
            @NotNull MentionCandidate candidate,
            @NotNull MentionView view
    ) {
        return original.copy().withStyle(style -> applyStyle(style, bold, italic));
    }
}
