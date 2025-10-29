package cn.qihuang02.cyyn.server.mention.function;

import cn.qihuang02.cyyn.api.mention.MentionFunction;
import cn.qihuang02.cyyn.util.mention.MentionTextUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

public abstract class BaseMentionFunction implements MentionFunction {
    protected static void appendStyledLiteral(@NotNull MutableComponent builder, @NotNull String text, @NotNull Style baseStyle) {
        if (text.isEmpty()) {
            return;
        }
        MutableComponent literal = Component.literal(text);
        if (!baseStyle.isEmpty()) {
            literal.setStyle(baseStyle);
        }
        builder.append(literal);
    }

    protected static MentionDecision keepOriginal() {
        return MentionDecision.KEEP;
    }

    protected static MentionDecision cancel() {
        return MentionDecision.CANCELLED;
    }

    protected static MentionDecision replace(@NotNull Component replacement) {
        return new MentionDecision(DecisionType.REPLACE, Objects.requireNonNull(replacement, "replacement"));
    }

    @Override
    public final @NotNull Result format(@NotNull ServerPlayer sender, @NotNull Component message) {
        String rawMessage = message.getString();
        if (rawMessage.isEmpty() || !rawMessage.contains("@")) {
            return Result.pass(name());
        }

        Style baseStyle = message.getStyle();
        MutableComponent rebuilt = Component.empty();
        if (!baseStyle.isEmpty()) {
            rebuilt.setStyle(baseStyle);
        }

        boolean replacedAny = false;
        int index = 0;
        for (MentionTextUtils.MentionTokenRange range : MentionTextUtils.scanMentions(rawMessage)) {
            appendStyledLiteral(rebuilt, rawMessage.substring(index, range.mentionStart()), baseStyle);

            String resolvedName = range.tokenIn(rawMessage);
            if (name().equalsIgnoreCase(resolvedName)) {
                MentionDecision decision = handleMention(sender, message, range);
                if (decision.type == DecisionType.CANCEL) {
                    return Result.cancel(name());
                }
                if (decision.type == DecisionType.REPLACE) {
                    rebuilt.append(decision.replacement);
                    replacedAny = true;
                } else {
                    appendStyledLiteral(rebuilt, range.mentionIn(rawMessage), baseStyle);
                }
            } else {
                appendStyledLiteral(rebuilt, range.mentionIn(rawMessage), baseStyle);
            }

            index = range.tokenEnd();
        }

        if (index < rawMessage.length()) {
            appendStyledLiteral(rebuilt, rawMessage.substring(index), baseStyle);
        }

        if (!replacedAny) {
            return Result.pass(name());
        }

        return Result.replace(name(), rebuilt);
    }

    protected abstract @NotNull MentionDecision handleMention(@NotNull ServerPlayer sender,
                                                              @NotNull Component message,
                                                              @NotNull MentionTextUtils.MentionTokenRange range);

    protected enum DecisionType {
        KEEP_ORIGINAL,
        REPLACE,
        CANCEL
    }

    protected static class MentionDecision {
        private static final MentionDecision KEEP = new MentionDecision(DecisionType.KEEP_ORIGINAL, Component.empty());
        private static final MentionDecision CANCELLED = new MentionDecision(DecisionType.CANCEL, Component.empty());

        private final DecisionType type;
        private final Component replacement;

        private MentionDecision(@NotNull DecisionType type, @NotNull Component replacement) {
            this.type = type;
            this.replacement = replacement;
        }
    }
}
