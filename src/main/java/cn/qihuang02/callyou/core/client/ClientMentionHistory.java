package cn.qihuang02.callyou.core.client;

import cn.qihuang02.callyou.core.saveddata.MentionRecord;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class ClientMentionHistory {
    private static List<MentionRecord> records = List.of();
    private static boolean awaitingResponse;
    private static long lastUpdateMillis;

    public static synchronized void markAwaitingResponse() {
        awaitingResponse = true;
    }

    public static synchronized void update(@NotNull List<MentionRecord> newRecords) {
        records = List.copyOf(newRecords);
        awaitingResponse = false;
        lastUpdateMillis = System.currentTimeMillis();
    }

    public static synchronized @NotNull List<MentionRecord> copy() {
        return new ArrayList<>(records);
    }

    public static synchronized boolean isAwaitingResponse() {
        return awaitingResponse;
    }

    public static synchronized long lastUpdateMillis() {
        return lastUpdateMillis;
    }
}
