package cn.qihuang02.callyou.core.mention.components.decorator;

import cn.qihuang02.callyou.api.MentionCandidate;
import cn.qihuang02.callyou.api.MentionContext;
import cn.qihuang02.callyou.api.MentionView;
import cn.qihuang02.callyou.api.components.InteractionDecorator;
import cn.qihuang02.callyou.registry.BuiltInCallYouRegistries;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import org.jetbrains.annotations.NotNull;

public enum RarityStyleDecorator implements InteractionDecorator {
    INSTANCE;

    public static final MapCodec<RarityStyleDecorator> MAP_CODEC = MapCodec.unit(INSTANCE);

    @Override
    public @NotNull InteractionDecoratorType type() {
        return BuiltInCallYouRegistries.RARITY_STYLE_DECORATOR_TYPE;
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
        Rarity rarity = stack.getRarity();
        return original.copy().withStyle(rarity.getStyleModifier());
    }
}
