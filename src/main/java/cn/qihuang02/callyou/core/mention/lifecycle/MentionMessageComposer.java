package cn.qihuang02.callyou.core.mention.lifecycle;

import cn.qihuang02.callyou.api.*;
import cn.qihuang02.callyou.api.components.TextFormatter;
import net.minecraft.network.chat.*;
import org.jetbrains.annotations.NotNull;

public final class MentionMessageComposer implements MentionLifeCycle {
    @Override
    public void process(@NotNull MentionContext context) {
        String raw = context.rawText();
        MutableComponent rebuilt = Component.literal("");
        MutableComponent rebuiltForSender = Component.literal("");
        int lastIndex = 0;

        for (MentionCandidate candidate : context.candidates()) {
            int start = candidate.startIndex();
            int end = candidate.endIndex();

            if (start < lastIndex || start >= raw.length() || end <= start) {
                continue;
            }

            if (start > lastIndex) {
                String before = raw.substring(lastIndex, start);
                if (!before.isEmpty()) {
                    rebuilt.append(before);
                    rebuiltForSender.append(before);
                }
            }

            String substring = raw.substring(start, Math.min(end, raw.length()));
            MentionType type = candidate.type();
            if (type == null) {
                rebuilt.append(substring);
                if (candidate.resolveStatus().isError()) {
                    rebuiltForSender.append(applySenderStatusStyle(Component.literal(substring), candidate));
                } else {
                    rebuiltForSender.append(substring);
                }
                lastIndex = end;
                continue;
            }

            if (candidate.resolveStatus().isError()) {
                rebuilt.append(substring);
                rebuiltForSender.append(applySenderStatusStyle(Component.literal(substring), candidate));
                lastIndex = end;
                continue;
            }

            TextFormatter formatter = type.textFormatter();
            Component formattedMention = formatter.format(context, candidate);
            Component mentionForTargets = applyReplyStyle(formatter, context, candidate, formattedMention);

            rebuilt.append(mentionForTargets);
            rebuiltForSender.append(applySenderStatusStyle(formattedMention, candidate));
            lastIndex = end;
        }

        if (lastIndex < raw.length()) {
            String tail = raw.substring(lastIndex);
            if (!tail.isEmpty()) {
                rebuilt.append(tail);
                rebuiltForSender.append(tail);
            }
        }

        context.setFinalMessage(rebuilt);
        context.setSenderView(rebuiltForSender);
    }

    private @NotNull Component applyReplyStyle(
            @NotNull TextFormatter formatter,
            @NotNull MentionContext context,
            @NotNull MentionCandidate candidate,
            @NotNull Component formattedMention
    ) {
        if (!formatter.supportReply()) {
            return formattedMention;
        }
        String suggestion = formatter.buildReplySuggestion(context, candidate, formattedMention);
        if (suggestion.isBlank()) {
            return formattedMention;
        }
        HoverEvent replyHover = new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                Component.translatable("message.callyou.reply.hover")
        );
        return formattedMention.copy().withStyle(style -> {
            Style updated = style.withClickEvent(
                    new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, suggestion)
            );
            if (style.getHoverEvent() == null) {
                updated = updated.withHoverEvent(replyHover);
            }
            return updated;
        });
    }

    private @NotNull Component applySenderStatusStyle(
            @NotNull Component base,
            @NotNull MentionCandidate candidate
    ) {
        if (candidate.deliveryStatus() == DeliveryStatus.RATE_LIMITED) {
            return base.copy().withStyle(style -> style.withColor(DeliveryStatus.RATE_LIMITED.getSenderColor()));
        }
        if (candidate.resolveStatus() != ResolveStatus.SUCCESS) {
            return base.copy().withStyle(style -> style.withColor(candidate.resolveStatus().getSenderColor()));
        }
        return base;
    }
}
