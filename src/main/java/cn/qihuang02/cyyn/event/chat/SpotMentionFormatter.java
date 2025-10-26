package cn.qihuang02.cyyn.event.chat;

import cn.qihuang02.cyyn.util.MentionTextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class SpotMentionFormatter implements MentionFormatter {
    @Override
    public @NotNull Result format(@NotNull ServerPlayer sender, @NotNull Component message) {
        String rawMessage = message.getString();
        if (rawMessage.isEmpty() || !rawMessage.contains("@")) {
            return Result.pass();
        }

        Style baseStyle = message.getStyle();
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
            if ("spot".equalsIgnoreCase(token)) {
                rebuilt.append(createSpotComponent(sender));
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

    private @NotNull MutableComponent createSpotComponent(@NotNull ServerPlayer sender) {
        BlockPos blockPos = sender.blockPosition();
        MutableComponent location = Component.translatable("message.cyyn.spot", blockPos.getX(), blockPos.getY(), blockPos.getZ());
        MutableComponent hoverText = Component.empty().append(location);

        return ComponentUtils.wrapInSquareBrackets(Component.literal("Spot"))
                .withStyle(style -> style
                        .withColor(ChatFormatting.GREEN)
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText)));
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
