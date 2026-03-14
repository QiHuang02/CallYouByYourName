package cn.qihuang02.callyou.core.mention.components.decorator;

import cn.qihuang02.callyou.api.MentionCandidate;
import cn.qihuang02.callyou.api.MentionContext;
import cn.qihuang02.callyou.api.MentionView;
import cn.qihuang02.callyou.api.components.InteractionDecorator;
import cn.qihuang02.callyou.registry.BuiltInCallYouRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public record TextColorDecorator(ChatFormatting color) implements InteractionDecorator {
    public static final MapCodec<TextColorDecorator> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.optionalFieldOf("color", "white")
                    .xmap(
                            s -> ChatFormatting.valueOf(s.toUpperCase(Locale.ROOT)),
                            ChatFormatting::getName
                    )
                    .forGetter(TextColorDecorator::color)
    ).apply(instance, TextColorDecorator::new));

    @Override
    public @NotNull InteractionDecoratorType type() {
        return BuiltInCallYouRegistries.TEXT_COLOR_DECORATOR_TYPE;
    }

    @Override
    public int priority() {
        return -100;
    }

    @Override
    public @NotNull Component decorate(
            @NotNull Component original,
            @NotNull MentionContext context,
            @NotNull MentionCandidate candidate,
            @NotNull MentionView view
    ) {
        return original.copy().withStyle(style -> style.withColor(this.color));
    }
}
