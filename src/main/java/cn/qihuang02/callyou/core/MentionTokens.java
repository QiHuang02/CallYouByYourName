package cn.qihuang02.callyou.core;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MentionTokens {
    private static final Pattern PATTERN = Pattern.compile("@([A-Za-z0-9_]+)");

    public record Token(int startIndex, int endIndex, @NotNull String key) {
    }

    public static @NotNull List<Token> scan(@NotNull String text) {
        if (text.isEmpty()) {
            return List.of();
        }

        List<Token> tokens = new ArrayList<>();
        Matcher matcher = PATTERN.matcher(text);
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            String key = matcher.group(1);
            tokens.add(new Token(start, end, key));
        }
        return tokens;
    }
}
