package cn.qihuang02.callyou.core.mention.components.decorator;

import cn.qihuang02.callyou.api.MentionCandidate;
import cn.qihuang02.callyou.api.MentionContext;
import cn.qihuang02.callyou.api.MentionView;
import cn.qihuang02.callyou.api.components.InteractionDecorator;
import cn.qihuang02.callyou.registry.BuiltInCallYouRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public record OpenUrlDecorator(String url) implements InteractionDecorator {
    public static final MapCodec<OpenUrlDecorator> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("url").forGetter(OpenUrlDecorator::url)
    ).apply(instance, OpenUrlDecorator::new));

    @Override
    public @NotNull InteractionDecoratorType type() {
        return BuiltInCallYouRegistries.OPEN_URL_DECORATOR_TYPE;
    }

    @Override
    public @NotNull Component decorate(
            @NotNull Component original,
            @NotNull MentionContext context,
            @NotNull MentionCandidate candidate,
            @NotNull MentionView view
    ) {
        ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.OPEN_URL, this.url);
        return original.copy().withStyle(style -> style.withClickEvent(clickEvent));
    }
}
