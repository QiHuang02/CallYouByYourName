package cn.qihuang02.cyyn.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

/**
 * Helper methods for parsing and working with mentions in chat text.
 */
public class MentionTextUtils {
    public static boolean isMentionChar(char ch) {
        return Character.isLetterOrDigit(ch) || ch == '_' || ch == '-';
    }

    public static @NotNull Optional<MentionTokenRange> findTokenRange(@Nullable String text, int fromIndex) {
        if (text == null || text.isEmpty()) {
            return Optional.empty();
        }
        int searchIndex = Math.max(0, Math.min(fromIndex, text.length()));
        while (searchIndex < text.length()) {
            int atIndex = text.indexOf('@', searchIndex);
            if (atIndex == -1) {
                break;
            }
            if (atIndex > 0 && isMentionChar(text.charAt(atIndex - 1))) {
                searchIndex = atIndex + 1;
                continue;
            }
            int tokenEnd = atIndex + 1;
            while (tokenEnd < text.length() && isMentionChar(text.charAt(tokenEnd))) {
                tokenEnd++;
            }
            if (tokenEnd > atIndex + 1) {
                return Optional.of(new MentionTokenRange(atIndex, tokenEnd));
            }
            searchIndex = atIndex + 1;
        }
        return Optional.empty();
    }

    public static @NotNull String normalizeToken(@NotNull String rawToken) {
        Objects.requireNonNull(rawToken, "rawToken");
        int start = 0;
        int end = rawToken.length();
        while (start < end && !isMentionChar(rawToken.charAt(start))) {
            start++;
        }
        while (end > start && !isMentionChar(rawToken.charAt(end - 1))) {
            end--;
        }
        if (start >= end) {
            return "";
        }
        return rawToken.substring(start, end);
    }

    public record MentionTokenRange(int mentionStart, int mentionEnd) {
        public int tokenStart() {
            return mentionStart + 1;
        }

        public int tokenEnd() {
            return mentionEnd;
        }

        public int mentionEnd() {
            return mentionEnd;
        }

        public @NotNull String tokenIn(@NotNull String text) {
            Objects.requireNonNull(text, "text");
            return text.substring(tokenStart(), tokenEnd());
        }

        public @NotNull String mentionIn(@NotNull String text) {
            Objects.requireNonNull(text, "text");
            return text.substring(mentionStart, mentionEnd);
        }
    }
}
