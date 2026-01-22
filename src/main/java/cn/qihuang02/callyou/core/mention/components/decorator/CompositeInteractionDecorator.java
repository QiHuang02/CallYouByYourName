package cn.qihuang02.callyou.core.mention.components.decorator;

import cn.qihuang02.callyou.api.MentionCandidate;
import cn.qihuang02.callyou.api.MentionContext;
import cn.qihuang02.callyou.api.MentionView;
import cn.qihuang02.callyou.api.components.InteractionDecorator;
import cn.qihuang02.callyou.registry.BuiltInCallYouRegistries;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public record CompositeInteractionDecorator(List<InteractionDecorator> decorators) implements InteractionDecorator {
    public static final MapCodec<CompositeInteractionDecorator> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            InteractionDecorator.CODEC.listOf().optionalFieldOf("decorators", List.of())
                    .forGetter(CompositeInteractionDecorator::decorators)
    ).apply(instance, CompositeInteractionDecorator::new));

    @Override
    public @NotNull InteractionDecoratorType type() {
        return BuiltInCallYouRegistries.COMPOSITE_DECORATOR_TYPE.get();
    }

    @Override
    public @NotNull Component decorate(
            @NotNull Component original,
            @NotNull MentionContext context,
            @NotNull MentionCandidate candidate,
            @NotNull MentionView view
    ) {
        Component result = original;
        List<InteractionDecorator> sorted = new ArrayList<>(this.decorators);
        sorted.sort(Comparator.comparingInt(InteractionDecorator::priority).reversed());
        for (InteractionDecorator decorator : sorted) {
            result = decorator.decorate(result, context, candidate, view);
        }
        return result;
    }
}
