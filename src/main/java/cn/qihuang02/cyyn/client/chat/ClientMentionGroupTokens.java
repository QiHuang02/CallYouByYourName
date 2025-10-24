package cn.qihuang02.cyyn.client.chat;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;

public final class ClientMentionGroupTokens {
    private static final Object LOCK = new Object();
    private static final List<String> TOKENS = new ArrayList<>();

    public static void updateTokens(@NotNull Collection<String> tokens) {
        synchronized (LOCK) {
            TOKENS.clear();
            for (String token : tokens) {
                if (token != null && !token.isEmpty()) {
                    TOKENS.add(token);
                }
            }
        }
    }

    public static @NotNull @UnmodifiableView List<String> getTokens() {
        synchronized (LOCK) {
            return Collections.unmodifiableList(new ArrayList<>());
        }
    }
}
