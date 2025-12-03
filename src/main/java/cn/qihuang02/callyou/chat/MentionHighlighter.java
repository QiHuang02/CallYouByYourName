package cn.qihuang02.callyou.chat;

import cn.qihuang02.callyou.core.MentionTokens;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class MentionHighlighter {
    private static final Style PLAYER_STYLE =
            Style.EMPTY.withColor(ChatFormatting.AQUA);

    private static final Style DEFAULT_MENTION_TYPE_STYLE =
            Style.EMPTY.withColor(ChatFormatting.LIGHT_PURPLE);

    private final Minecraft minecraft;
    private final ClientMentionContext mentionContext;
    private final MentionCandidateProvider candidateProvider;

    public MentionHighlighter(@NotNull Minecraft minecraft,
                              @NotNull ClientMentionContext mentionContext,
                              @NotNull MentionCandidateProvider candidateProvider) {
        this.minecraft = minecraft;
        this.mentionContext = mentionContext;
        this.candidateProvider = candidateProvider;
    }

    public @NotNull FormattedCharSequence format(@NotNull String text, int cursorPosition) {
        List<MentionTokens.Token> tokens = MentionTokens.scan(text);
        if (tokens.isEmpty()) {
            return Component.literal(text).getVisualOrderText();
        }

        Set<String> mentionTypeKeys = mentionContext.getMentionTypeKeys();
        Map<String, Style> mentionTypeStyles = mentionContext.getMentionTypeStyles();
        Set<String> playerNames = new HashSet<>(candidateProvider.getPlayerCandidates());

        MutableComponent result = Component.empty();
        int lastIndex = 0;

        for (MentionTokens.Token token : tokens) {
            int start = token.startIndex();
            int end = token.endIndex();
            String key = token.key();

            if (start > lastIndex) {
                result.append(Component.literal(text.substring(lastIndex, start)));
            }

            Style style = getStyleForKey(key, mentionTypeKeys, mentionTypeStyles, playerNames);
            String fullToken = text.substring(start, end); // 包含 @

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

    private static @NotNull Style getStyleForKey(@NotNull String key,
                                                 @NotNull Set<String> mentionTypeKeys,
                                                 @NotNull Map<String, Style> mentionTypeStyles,
                                                 @NotNull Set<String> playerNames) {
        String lowered = key.toLowerCase(Locale.ROOT);

        if (mentionTypeKeys.contains(lowered)) {
            return mentionTypeStyles.getOrDefault(lowered, DEFAULT_MENTION_TYPE_STYLE);
        }
        if (playerNames.contains(key)) {
            return PLAYER_STYLE;
        }
        return Style.EMPTY;
    }
}
