package cn.qihuang02.cyyn.server.mention;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MentionCooldownTracker {
    private static final MentionCooldownTracker INSTANCE = new MentionCooldownTracker();

    private final Map<UUID, Long> playerCooldownMap = new ConcurrentHashMap<>();

    public static @NotNull MentionCooldownTracker getInstance() {
        return INSTANCE;
    }

    public void clearCooldown(@NotNull UUID playerId) {
        playerCooldownMap.remove(playerId);
    }

    public void clearAllCooldowns() {
        playerCooldownMap.clear();
    }

    public boolean isOnCooldown(@NotNull UUID playerId, long cooldownMs, long currentTime) {
        return getRemainingMillis(playerId, cooldownMs, currentTime) > 0;
    }

    public long getRemainingSeconds(@NotNull UUID playerId, long cooldownMs, long currentTime) {
        long remainingMillis = getRemainingMillis(playerId, cooldownMs, currentTime);
        if (remainingMillis <= 0) {
            return 0;
        }
        return remainingMillis / 1000L;
    }

    public void updateCooldown(@NotNull UUID playerId, long currentTime) {
        playerCooldownMap.put(playerId, currentTime);
    }

    private long getRemainingMillis(@NotNull UUID playerId, long cooldownMs, long currentTime) {
        long lastAtTime = playerCooldownMap.getOrDefault(playerId, 0L);
        long elapsed = currentTime - lastAtTime;
        return cooldownMs - elapsed;
    }
}
