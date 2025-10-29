package cn.qihuang02.cyyn.util.mention;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Helper methods for parsing and working with mentions in chat text.
 */
public class MentionTextUtils {
    private MentionTextUtils() {
    }

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
            return Optional.of(new MentionTokenRange(atIndex, tokenEnd));
        }
        return Optional.empty();
    }

    public static @NotNull Iterable<MentionTokenRange> scanMentions(@Nullable String text) {
        return scanMentions(text, 0);
    }

    public static @NotNull Iterable<MentionTokenRange> scanMentions(@Nullable String text, int fromIndex) {
        if (text == null || text.isEmpty()) {
            return Collections::emptyIterator;
        }
        int start = Math.max(0, Math.min(fromIndex, text.length()));
        return () -> new MentionTokenScanner(text, start);
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

    private static final class MentionTokenScanner implements Iterator<MentionTokenRange> {
        private final String text;
        private int searchIndex;
        private MentionTokenRange nextRange;
        private boolean prepared;

        private MentionTokenScanner(@NotNull String text, int startIndex) {
            this.text = text;
            this.searchIndex = startIndex;
        }

        @Override
        public boolean hasNext() {
            prepareNext();
            return this.nextRange != null;
        }

        @Override
        public MentionTokenRange next() {
            prepareNext();
            if (this.nextRange == null) {
                throw new NoSuchElementException();
            }
            MentionTokenRange current = this.nextRange;
            this.searchIndex = current.tokenEnd();
            this.prepared = false;
            this.nextRange = null;
            return current;
        }

        private void prepareNext() {
            if (this.prepared) {
                return;
            }
            this.prepared = true;
            Optional<MentionTokenRange> optional = MentionTextUtils.findTokenRange(this.text, this.searchIndex);
            this.nextRange = optional.orElse(null);
        }
    }
}
