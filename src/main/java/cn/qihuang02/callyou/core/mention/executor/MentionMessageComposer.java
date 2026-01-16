package cn.qihuang02.callyou.core.mention.executor;

import cn.qihuang02.callyou.api.MentionContext;
import cn.qihuang02.callyou.api.MentionType;
import cn.qihuang02.callyou.api.components.TextFormatter;
import cn.qihuang02.callyou.core.MentionResolver;
import cn.qihuang02.callyou.core.mention.executor.MentionResult.MentionEntry;
import cn.qihuang02.callyou.core.mention.executor.MentionResult.MentionStatus;
import cn.qihuang02.callyou.core.mention.components.formatter.ItemTextFormatter;
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
            @NotNull List<MentionEntry> entries
    ) {
        MutableComponent rebuilt = Component.literal("");
        MutableComponent rebuiltForSender = Component.literal("");
        int lastIndex = 0;

        List<MentionExecution> pendingMentions = new ArrayList<>();
        boolean itemMentionUsed = false;

        for (MentionEntry entry : entries) {
            MentionResolver.ResolvedMention parsed = entry.mention();
            int start = parsed.startIndex();
            int end = parsed.endIndex();

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
            MentionStatus status = entry.status();
            MentionType type = parsed.mentionType();
            if (!status.isAllowed() || type == null) {
                rebuilt.append(substring);
                rebuiltForSender.append(substring);
                lastIndex = end;
                continue;
            }
            if (isItemMention(type)) {
                if (itemMentionUsed) {
                    rebuilt.append(substring);
                    rebuiltForSender.append(substring);
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
            Component mentionForTargets = applyReplyStyle(formatter, context, formattedMention);

            rebuilt.append(mentionForTargets);
            rebuiltForSender.append(formattedMention);
            pendingMentions.add(new MentionExecution(type, context, status));

            lastIndex = end;
        }

        if (lastIndex < raw.length()) {
            String tail = raw.substring(lastIndex);
            if (!tail.isEmpty()) {
                rebuilt.append(tail);
                rebuiltForSender.append(tail);
            }
        }

        return new ComposeResult(rebuilt, rebuiltForSender, pendingMentions);
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

    private boolean isItemMention(@NotNull MentionType type) {
        return type.textFormatter() instanceof ItemTextFormatter;
    }

    public record ComposeResult(
            @NotNull Component rebuilt,
            @NotNull Component senderView,
            @NotNull List<MentionExecution> pendingMentions
    ) {
    }
}
