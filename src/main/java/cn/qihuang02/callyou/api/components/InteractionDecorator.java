package cn.qihuang02.callyou.api.components;

import cn.qihuang02.callyou.api.MentionCandidate;
import cn.qihuang02.callyou.api.MentionContext;
import cn.qihuang02.callyou.api.MentionView;
import cn.qihuang02.callyou.registry.CallYouRegistries;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface InteractionDecorator extends IDispatchedComponent<InteractionDecorator, InteractionDecorator.InteractionDecoratorType> {
    Codec<InteractionDecorator> CODEC = createCodec();

    private static Codec<InteractionDecorator> createCodec() {
        Codec<InteractionDecorator> dispatched = IDispatchedComponent.codec(CallYouRegistries.INTERACTION_DECORATOR_TYPES);
        return new Codec<>() {
            @Override
            public <T> DataResult<Pair<InteractionDecorator, T>> decode(DynamicOps<T> ops, T input) {
                DataResult<String> asString = ops.getStringValue(input);
                if (asString.result().isPresent()) {
                    return ops.mapBuilder()
                            .add("type", input)
                            .build(ops.emptyMap())
                            .flatMap(map -> dispatched.decode(ops, map));
                }
                return dispatched.decode(ops, input);
            }

            @Override
            public <T> DataResult<T> encode(InteractionDecorator input, DynamicOps<T> ops, T prefix) {
                return dispatched.encode(input, ops, prefix);
            }
        };
    }

    @NotNull Component decorate(
            @NotNull Component original,
            @NotNull MentionContext context,
            @NotNull MentionCandidate candidate,
            @NotNull MentionView view
    );

    default int priority() {
        return 0;
    }

    @Override
    default @NotNull InteractionDecoratorType type() {
        throw new UnsupportedOperationException("InteractionDecorator.type() must be overridden");
    }

    record InteractionDecoratorType(MapCodec<? extends InteractionDecorator> mapCodec)
            implements IDispatchedComponent.Type<InteractionDecorator> {
    }
}
