package cn.qihuang02.callyou.core.mention.executor;

import cn.qihuang02.callyou.api.MentionContext;
import cn.qihuang02.callyou.api.MentionType;
import cn.qihuang02.callyou.api.components.TextFormatter;
import cn.qihuang02.callyou.core.MentionResolver;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class MentionMessageComposer {
    public @NotNull ComposeResult compose(
            @NotNull ServerPlayer sender,
            @NotNull Component originalMessage,
            @NotNull String raw,
            @NotNull List<MentionResolver.ResolvedMention> mentions
    ) {
        MutableComponent rebuilt = Component.literal("");
        int lastIndex = 0;

        List<MentionExecution> pendingMentions = new ArrayList<>();
        boolean itemMentionUsed = false;

        for (MentionResolver.ResolvedMention parsed : mentions) {
            int start = parsed.startIndex();
            int end = parsed.endIndex();

            if (start < lastIndex || start >= raw.length() || end <= start) {
                continue;
            }

            if (start > lastIndex) {
                String before = raw.substring(lastIndex, start);
                if (!before.isEmpty()) {
                    rebuilt.append(before);
                }
            }

            MentionType type = parsed.mentionType();
            String substring = raw.substring(start, Math.min(end, raw.length()));
            if (type == null) {
                rebuilt.append(substring);
                lastIndex = end;
                continue;
            }
            if (MentionExecutionSupport.isItemMention(type)) {
                if (itemMentionUsed) {
                    rebuilt.append(substring);
                    lastIndex = end;
                    continue;
                }
                itemMentionUsed = true;
            }

            ResourceLocation typeId = parsed.typeId();

            MentionContext context = new MentionContext(
                    sender,
                    originalMessage,
                    raw,
                    parsed.key(),
                    typeId
            );

            TextFormatter formatter = type.textFormatter();

            Component formattedMention = formatter.format(context);
            formattedMention = applyReplyStyle(formatter, context, formattedMention);

            rebuilt.append(formattedMention);
            pendingMentions.add(new MentionExecution(type, context));

            lastIndex = end;
        }

        if (lastIndex < raw.length()) {
            String tail = raw.substring(lastIndex);
            if (!tail.isEmpty()) {
                rebuilt.append(tail);
            }
        }

        return new ComposeResult(rebuilt, pendingMentions);
    }

    private @NotNull Component applyReplyStyle(
            @NotNull TextFormatter formatter,
            @NotNull MentionContext context,
            @NotNull Component formattedMention
    ) {
        if (!formatter.supportReply()) {
            return formattedMention;
        }
        String suggestion = formatter.buildReplySuggestion(context, formattedMention);
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

    public record ComposeResult(@NotNull Component rebuilt, @NotNull List<MentionExecution> pendingMentions) {
    }
}
