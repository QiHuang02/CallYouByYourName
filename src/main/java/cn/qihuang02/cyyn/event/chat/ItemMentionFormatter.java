package cn.qihuang02.cyyn.event.chat;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemMentionFormatter {
    public record FormatResult(@Nullable Component component, boolean canceled) {
    }

    @NotNull
    public FormatResult format(@NotNull ServerPlayer sender, @NotNull Component message) {
        String rawMessage = message.getString();
        if (rawMessage.isEmpty() || !rawMessage.contains("@")) {
            return new FormatResult(message, false);
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
            int atIndex = rawMessage.indexOf('@', index);
            if (atIndex == -1) {
                break;
            }

            appendStyledLiteral(rebuilt, rawMessage.substring(index, atIndex), baseStyle);

            if (atIndex + 1 >= rawMessage.length()) {
                appendStyledLiteral(rebuilt, "@", baseStyle);
                index = atIndex + 1;
                continue;
            }

            if (atIndex > 0 && isMentionChar(rawMessage.charAt(atIndex - 1))) {
                appendStyledLiteral(rebuilt, "@", baseStyle);
                index = atIndex + 1;
                continue;
            }

            int tokenEnd = atIndex + 1;
            while (tokenEnd < rawMessage.length() && isMentionChar(rawMessage.charAt(tokenEnd))) {
                tokenEnd++;
            }

            if (tokenEnd == atIndex + 1) {
                appendStyledLiteral(rebuilt, "@", baseStyle);
                index = tokenEnd;
                continue;
            }

            String token = rawMessage.substring(atIndex + 1, tokenEnd);
            if ("item".equalsIgnoreCase(token)) {
                if (mainHandItem.isEmpty()) {
                    sender.sendSystemMessage(
                            Component.translatable("message.cyyn.item.empty").withStyle(ChatFormatting.RED)
                    );
                    return new FormatResult(null, true);
                }

                rebuilt.append(createItemComponent(mainHandItem));
                replacedAny = true;
            } else {
                appendStyledLiteral(rebuilt, rawMessage.substring(atIndex, tokenEnd), baseStyle);
            }

            index = tokenEnd;
        }

        if (index < rawMessage.length()) {
            appendStyledLiteral(rebuilt, rawMessage.substring(index), baseStyle);
        }

        if (!replacedAny) {
            return new FormatResult(message, false);
        }

        return new FormatResult(rebuilt, true);
    }

    private boolean isMentionChar(char ch) {
        return Character.isLetterOrDigit(ch) || ch == '_';
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
