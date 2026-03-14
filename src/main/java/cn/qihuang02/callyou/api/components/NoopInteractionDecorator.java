package cn.qihuang02.callyou.api.components;

import cn.qihuang02.callyou.api.MentionCandidate;
import cn.qihuang02.callyou.api.MentionContext;
import cn.qihuang02.callyou.api.MentionView;
import cn.qihuang02.callyou.registry.BuiltInCallYouRegistries;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public enum NoopInteractionDecorator implements InteractionDecorator {
    INSTANCE;

    public static final MapCodec<NoopInteractionDecorator> MAP_CODEC = MapCodec.unit(INSTANCE);

    @Override
    public @NotNull InteractionDecoratorType type() {
        return BuiltInCallYouRegistries.NOOP_DECORATOR_TYPE;
    }

    @Override
    public @NotNull Component decorate(
            @NotNull Component original,
            @NotNull MentionContext context,
            @NotNull MentionCandidate candidate,
            @NotNull MentionView view
    ) {
        return original;
    }
}
