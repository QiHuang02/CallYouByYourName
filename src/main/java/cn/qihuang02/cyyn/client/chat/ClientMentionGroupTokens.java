package cn.qihuang02.cyyn.client.chat;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class ClientMentionGroupTokens {
    private static final Object LOCK = new Object();
    private static final List<String> TOKENS = new ArrayList<>();
    private static int REVISION;

    public static void updateTokens(@NotNull Collection<String> tokens) {
        synchronized (LOCK) {
            TOKENS.clear();
            for (String token : tokens) {
                if (token != null && !token.isEmpty()) {
                    TOKENS.add(token);
                }
            }
            REVISION++;
        }
    }

    public static @NotNull @UnmodifiableView List<String> getTokens() {
        synchronized (LOCK) {
            return List.copyOf(TOKENS);
        }
    }

    public static int getRevision() {
        synchronized (LOCK) {
            return REVISION;
        }
    }
}
