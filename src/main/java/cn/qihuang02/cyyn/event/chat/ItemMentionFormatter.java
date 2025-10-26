package cn.qihuang02.cyyn.event.chat;

import cn.qihuang02.cyyn.util.MentionTextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ItemMentionFormatter implements MentionFormatter {
    @Override
    public @NotNull Result format(@NotNull ServerPlayer sender, @NotNull Component message) {
        String rawMessage = message.getString();
        if (rawMessage.isEmpty() || !rawMessage.contains("@")) {
            return Result.pass();
        }

        Style baseStyle = message.getStyle();
        ItemStack mainHandItem = sender.getMainHandItem();
        MutableComponent rebuilt = Component.empty();
        if (!baseStyle.isEmpty()) {
            rebuilt.setStyle(baseStyle);
        }

        boolean replacedAny = false;
        int index = 0;
        while (index < rawMessage.length()) {
            Optional<MentionTextUtils.MentionTokenRange> rangeOptional = MentionTextUtils.findTokenRange(rawMessage, index);
            if (rangeOptional.isEmpty()) {
                break;
            }

            MentionTextUtils.MentionTokenRange range = rangeOptional.get();
            appendStyledLiteral(rebuilt, rawMessage.substring(index, range.mentionStart()), baseStyle);

            String token = range.tokenIn(rawMessage);
            if ("item".equalsIgnoreCase(token)) {
                if (mainHandItem.isEmpty()) {
                    sender.sendSystemMessage(
                            Component.translatable("message.cyyn.item.empty").withStyle(ChatFormatting.RED)
                    );
                    return Result.cancel();
                }

                rebuilt.append(createItemComponent(mainHandItem));
                replacedAny = true;
            } else {
                appendStyledLiteral(rebuilt, range.mentionIn(rawMessage), baseStyle);
            }

            index = range.tokenEnd();
        }

        if (index < rawMessage.length()) {
            appendStyledLiteral(rebuilt, rawMessage.substring(index), baseStyle);
        }

        if (!replacedAny) {
            return Result.pass();
        }

        return Result.replace(rebuilt);
    }

    @NotNull
    private MutableComponent createItemComponent(@NotNull ItemStack stack) {
        MutableComponent itemName = ComponentUtils.wrapInSquareBrackets(stack.getHoverName().copy());
        HoverEvent hoverEvent = new HoverEvent(
                HoverEvent.Action.SHOW_ITEM,
                new HoverEvent.ItemStackInfo(stack.copy())
        );

        return itemName.withStyle(style -> style
                .withHoverEvent(hoverEvent)
                .withColor(stack.getRarity().color)
                .withInsertion(stack.getDescriptionId())
        );
    }

    private void appendStyledLiteral(@NotNull MutableComponent builder, @NotNull String text, @NotNull Style baseStyle) {
        if (text.isEmpty()) {
            return;
        }
        MutableComponent literal = Component.literal(text);
        if (!baseStyle.isEmpty()) {
            literal.setStyle(baseStyle);
        }
        builder.append(literal);
    }
}
