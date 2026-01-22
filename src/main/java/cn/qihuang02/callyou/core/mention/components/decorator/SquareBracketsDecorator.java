package cn.qihuang02.callyou.core.mention.components.decorator;

import cn.qihuang02.callyou.api.MentionCandidate;
import cn.qihuang02.callyou.api.MentionContext;
import cn.qihuang02.callyou.api.MentionView;
import cn.qihuang02.callyou.api.components.InteractionDecorator;
import cn.qihuang02.callyou.registry.BuiltInCallYouRegistries;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import org.jetbrains.annotations.NotNull;

public enum SquareBracketsDecorator implements InteractionDecorator {
    INSTANCE;

    public static final MapCodec<SquareBracketsDecorator> MAP_CODEC = MapCodec.unit(INSTANCE);

    @Override
    public @NotNull InteractionDecoratorType type() {
        return BuiltInCallYouRegistries.SQUARE_BRACKETS_DECORATOR_TYPE.get();
    }

    @Override
    public int priority() {
        return 100;
    }

    @Override
    public @NotNull Component decorate(
            @NotNull Component original,
            @NotNull MentionContext context,
            @NotNull MentionCandidate candidate,
            @NotNull MentionView view
    ) {
        return ComponentUtils.wrapInSquareBrackets(original);
    }
}
