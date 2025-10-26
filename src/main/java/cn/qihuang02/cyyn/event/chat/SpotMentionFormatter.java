package cn.qihuang02.cyyn.event.chat;

import cn.qihuang02.cyyn.util.MentionTextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Optional;

public class SpotMentionFormatter implements MentionFormatter{
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
            if ("site".equalsIgnoreCase(token)) {
                rebuilt.append(createSiteComponent(sender));
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

    private @NotNull MutableComponent createSiteComponent(@NotNull ServerPlayer sender) {
        BlockPos blockPos = sender.blockPosition();
        ResourceKey<Level> dimension = sender.level().dimension();
        MutableComponent dimensionName = Component.translatable(
                String.format(Locale.ROOT, "dimension.%s.%s", dimension.location().getNamespace(), dimension.location().getPath())
        );
        MutableComponent coordinates = Component.literal(String.format(Locale.ROOT, "%d, %d, %d - ", blockPos.getX(), blockPos.getY(), blockPos.getZ()));
        MutableComponent payload = coordinates.append(dimensionName);
        return ComponentUtils.wrapInSquareBrackets(payload).withStyle(style -> style.withColor(ChatFormatting.GREEN));
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
