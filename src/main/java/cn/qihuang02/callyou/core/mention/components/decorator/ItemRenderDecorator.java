package cn.qihuang02.callyou.core.mention.components.decorator;

import cn.qihuang02.callyou.api.MentionCandidate;
import cn.qihuang02.callyou.api.MentionContext;
import cn.qihuang02.callyou.api.MentionView;
import cn.qihuang02.callyou.api.components.InteractionDecorator;
import cn.qihuang02.callyou.registry.BuiltInCallYouRegistries;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public enum ItemRenderDecorator implements InteractionDecorator {
    INSTANCE;

    public static final MapCodec<ItemRenderDecorator> MAP_CODEC = MapCodec.unit(INSTANCE);

    @Override
    public @NotNull InteractionDecoratorType type() {
        return BuiltInCallYouRegistries.ITEM_RENDER_DECORATOR_TYPE.get();
    }

    @Override
    public @NotNull Component decorate(
            @NotNull Component original,
            @NotNull MentionContext context,
            @NotNull MentionCandidate candidate,
            @NotNull MentionView view
    ) {
        ServerPlayer sender = context.sender();
        if (sender == null) {
            return original;
        }
        ItemStack stack = sender.getMainHandItem();
        if (stack.isEmpty()) {
            return original;
        }
        ItemStack copy = stack.copy();
        HoverEvent hover = new HoverEvent(
                HoverEvent.Action.SHOW_ITEM,
                new HoverEvent.ItemStackInfo(copy)
        );
        return original.copy().withStyle(style -> style
                .withHoverEvent(hover)
                .withInsertion(copy.getDescriptionId()));
    }
}
