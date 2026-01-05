package cn.qihuang02.callyou.client;

import cn.qihuang02.callyou.core.attachment.MentionPreferences;
import org.jetbrains.annotations.NotNull;

public final class ClientMentionPreferences {
    private static MentionPreferences preferences = new MentionPreferences();
    private static boolean syncPending;
    private static boolean awaitingSync;
    private static long lastSyncMillis;

    public static synchronized @NotNull MentionPreferences get() {
        return preferences;
    }

    public static synchronized @NotNull MentionPreferences copy() {
        MentionPreferences copy = new MentionPreferences();
        copy.copyFrom(preferences);
        return copy;
    }

    public static synchronized void update(@NotNull MentionPreferences newPreferences) {
        MentionPreferences copy = new MentionPreferences();
        copy.copyFrom(newPreferences);
        preferences = copy;
        syncPending = false;
        awaitingSync = false;
        lastSyncMillis = System.currentTimeMillis();
    }

    public static synchronized void markPending(@NotNull MentionPreferences stagedPreferences) {
        MentionPreferences copy = new MentionPreferences();
        copy.copyFrom(stagedPreferences);
        preferences = copy;
        syncPending = true;
        awaitingSync = false;
    }

    public static synchronized boolean isSyncPending() {
        return syncPending;
    }

    public static synchronized void markAwaitingSync() {
        awaitingSync = true;
    }

    public static synchronized boolean isAwaitingSync() {
        return awaitingSync;
    }

    public static synchronized long getLastSyncMillis() {
        return lastSyncMillis;
    }
}
