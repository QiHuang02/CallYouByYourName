package cn.qihuang02.callyou.client.chat;

import cn.qihuang02.callyou.core.MentionTokens;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public record MentionHighlighter(Minecraft minecraft, ClientMentionContext mentionContext,
                                 MentionCandidateProvider candidateProvider) {
    public MentionHighlighter(@NotNull Minecraft minecraft,
                              @NotNull ClientMentionContext mentionContext,
                              @NotNull MentionCandidateProvider candidateProvider) {
        this.minecraft = minecraft;
        this.mentionContext = mentionContext;
        this.candidateProvider = candidateProvider;
    }

    private static @NotNull Style getStyleForKey(@NotNull String key,
                                                 @NotNull Set<String> mentionTypeKeys,
                                                 @NotNull Map<String, Style> mentionTypeStyles,
                                                 @NotNull Set<String> playerNames,
                                                 @NotNull Style playerStyle) {
        String lowered = key.toLowerCase(Locale.ROOT);

        if (mentionTypeKeys.contains(lowered)) {
            Style style = mentionTypeStyles.get(lowered);
            return style == null ? Style.EMPTY : style;
        }

        if (playerNames.contains(key)) {
            return playerStyle;
        }

        return Style.EMPTY;
    }

    public @NotNull FormattedCharSequence format(@NotNull String text, int cursorPosition) {
        List<MentionTokens.Token> tokens = MentionTokens.scan(text);
        if (tokens.isEmpty()) {
            return Component.literal(text).getVisualOrderText();
        }

        Set<String> mentionTypeKeys = mentionContext.getMentionTypeKeys();
        Map<String, Style> mentionTypeStyles = mentionContext.getMentionTypeStyles();
        Set<String> playerNames = new HashSet<>(candidateProvider.getPlayerCandidates());
        Style playerStyle = mentionContext.getPlayerMentionStyle();

        MutableComponent result = Component.empty();
        int lastIndex = 0;

        for (MentionTokens.Token token : tokens) {
            int start = token.startIndex();
            int end = token.endIndex();
            String key = token.key();

            if (start > lastIndex) {
                result.append(Component.literal(text.substring(lastIndex, start)));
            }

            Style style = getStyleForKey(key, mentionTypeKeys, mentionTypeStyles, playerNames, playerStyle);
            String fullToken = text.substring(start, end);

            if (style.isEmpty()) {
                result.append(Component.literal(fullToken));
            } else {
                result.append(Component.literal(fullToken).withStyle(style));
            }

            lastIndex = end;
        }

        if (lastIndex < text.length()) {
            result.append(Component.literal(text.substring(lastIndex)));
        }

        return result.getVisualOrderText();
    }
}
