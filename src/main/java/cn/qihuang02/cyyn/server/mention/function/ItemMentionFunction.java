package cn.qihuang02.cyyn.server.mention.function;

import cn.qihuang02.cyyn.util.mention.MentionTextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemMentionFunction extends BaseMentionFunction {
    @Override
    public @NotNull String name() {
        return "item";
    }

    @Override
    protected @NotNull MentionDecision handleMention(@NotNull ServerPlayer sender,
                                                     @NotNull Component message,
                                                     @NotNull MentionTextUtils.MentionTokenRange range) {
        ItemStack mainHandItem = sender.getMainHandItem();
        if (mainHandItem.isEmpty()) {
            sender.sendSystemMessage(
                    Component.translatable("message.cyyn.item.empty").withStyle(ChatFormatting.RED)
            );
            return cancel();
        }
        return replace(createItemComponent(mainHandItem));
    }

    @NotNull
    private MutableComponent createItemComponent(@NotNull ItemStack stack) {
        MutableComponent hoverName = stack.getHoverName().copy();
        HoverEvent hoverEvent = new HoverEvent(
                HoverEvent.Action.SHOW_ITEM,
                new HoverEvent.ItemStackInfo(stack.copy())
        );

        Style itemStyle = Style.EMPTY
                .withHoverEvent(hoverEvent)
                .withColor(stack.getRarity().color)
                .withInsertion(stack.getDescriptionId());

        MutableComponent padded = Component.literal("  ")
                .withStyle(style -> style
                        .withHoverEvent(null)
                        .withClickEvent(null)
                        .withInsertion(null)
                );
        MutableComponent styledName = hoverName.withStyle(existing -> existing
                .withHoverEvent(hoverEvent)
                .withColor(stack.getRarity().color)
                .withInsertion(stack.getDescriptionId())
        );

        MutableComponent wrapped = ComponentUtils.wrapInSquareBrackets(styledName)
                .withStyle(existing -> existing
                        .withHoverEvent(hoverEvent)
                        .withColor(stack.getRarity().color)
                        .withInsertion(stack.getDescriptionId())
                );

        return padded.append(wrapped);
    }
}
