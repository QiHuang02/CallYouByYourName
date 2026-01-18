package cn.qihuang02.callyou.core.mention.components.formatter;

import cn.qihuang02.callyou.api.MentionCandidate;
import cn.qihuang02.callyou.api.MentionContext;
import cn.qihuang02.callyou.api.components.TextFormatter;
import cn.qihuang02.callyou.registry.BuiltInCallYouRegistries;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import org.jetbrains.annotations.NotNull;

public enum ItemTextFormatter implements TextFormatter {
    INSTANCE;

    public static final MapCodec<ItemTextFormatter> MAP_CODEC = MapCodec.unit(INSTANCE);

    @Override
    public @NotNull TextFormatterType type() {
        return BuiltInCallYouRegistries.ITEM_FORMATTER_TYPE.get();
    }

    @Override
    public @NotNull Component format(@NotNull MentionContext context, @NotNull MentionCandidate candidate) {
        ServerPlayer sender = context.sender();
        if (sender == null) {
            return Component.literal("@item");
        }

        ItemStack stack = sender.getMainHandItem();
        if (stack.isEmpty()) {
            return Component.literal(candidate.mentionToken());
        }

        ItemStack copy = stack.copy();
        Rarity rarity = copy.getRarity();

        HoverEvent hover = new HoverEvent(
                HoverEvent.Action.SHOW_ITEM,
                new HoverEvent.ItemStackInfo(copy)
        );

        MutableComponent nameComponent = copy.getHoverName().copy();

        MutableComponent styledName = nameComponent.withStyle(existing -> {
            var base = rarity.getStyleModifier().apply(existing);
            return base
                    .withHoverEvent(hover)
                    .withInsertion(copy.getDescriptionId());
        });

        MutableComponent bracketed = ComponentUtils
                .wrapInSquareBrackets(styledName)
                .withStyle(existing -> {
                    var base = rarity.getStyleModifier().apply(existing);
                    return base
                            .withHoverEvent(hover)
                            .withInsertion(copy.getDescriptionId());
                });

        return bracketed;
    }
}
