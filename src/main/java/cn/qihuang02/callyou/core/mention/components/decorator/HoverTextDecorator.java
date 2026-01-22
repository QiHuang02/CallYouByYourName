package cn.qihuang02.callyou.core.mention.components.decorator;

import cn.qihuang02.callyou.api.MentionCandidate;
import cn.qihuang02.callyou.api.MentionContext;
import cn.qihuang02.callyou.api.MentionView;
import cn.qihuang02.callyou.api.components.InteractionDecorator;
import cn.qihuang02.callyou.registry.BuiltInCallYouRegistries;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.HoverEvent;
import org.jetbrains.annotations.NotNull;

public record HoverTextDecorator(Component text) implements InteractionDecorator {
    public static final MapCodec<HoverTextDecorator> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ComponentSerialization.CODEC.optionalFieldOf("text", Component.empty()).forGetter(HoverTextDecorator::text)
    ).apply(instance, HoverTextDecorator::new));

    @Override
    public @NotNull InteractionDecoratorType type() {
        return BuiltInCallYouRegistries.HOVER_TEXT_DECORATOR_TYPE.get();
    }

    @Override
    public @NotNull Component decorate(
            @NotNull Component original,
            @NotNull MentionContext context,
            @NotNull MentionCandidate candidate,
            @NotNull MentionView view
    ) {
        HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, this.text);
        return original.copy().withStyle(style -> style.withHoverEvent(hoverEvent));
    }
}
