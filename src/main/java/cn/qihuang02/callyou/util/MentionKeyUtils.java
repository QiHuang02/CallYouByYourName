package cn.qihuang02.callyou.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public final class MentionKeyUtils {
    public static @NotNull String normalize(@Nullable String key) {
        if (key == null) {
            return "";
        }
        return key.toLowerCase(Locale.ROOT);
    }

    public static boolean matches(@Nullable String a, @Nullable String b) {
        if (a == null || b == null) {
            return false;
        }
        return normalize(a).equals(normalize(b));
    }
}
