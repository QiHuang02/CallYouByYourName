package cn.qihuang02.cyyn.client.chat;

import cn.qihuang02.cyyn.CallYouByYourName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class ClientMentionGroupNames {
    private static final Object LOCK = new Object();
    private static final List<String> NAMES = new ArrayList<>();
    private static int REVISION;

    public static void updateNames(@NotNull Collection<String> names) {
        synchronized (LOCK) {
            NAMES.clear();
            for (String name : names) {
                if (name != null && !name.isEmpty()) {
                    NAMES.add(name);
                }
            }
            REVISION++;
            CallYouByYourName.LOGGER.info("Client mention group names updated: {}", NAMES);
        }
    }

    public static @NotNull @UnmodifiableView List<String> getNames() {
        synchronized (LOCK) {
            return List.copyOf(NAMES);
        }
    }

    public static int getRevision() {
        synchronized (LOCK) {
            return REVISION;
        }
    }
}
