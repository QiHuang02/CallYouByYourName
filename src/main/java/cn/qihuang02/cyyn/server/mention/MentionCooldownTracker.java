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

    public boolean isOnCooldown(@NotNull UUID playerId, long cooldownTicks, long currentTick) {
        return getRemainingTicks(playerId, cooldownTicks, currentTick) > 0;
    }

    public long getRemainingTicks(@NotNull UUID playerId, long cooldownTicks, long currentTick) {
        if (cooldownTicks <= 0) {
            return 0;
        }

        Long lastProcessedTick = playerCooldownMap.get(playerId);
        if (lastProcessedTick == null) {
            return 0;
        }

        long elapsed = currentTick - lastProcessedTick;
        if (elapsed < 0) {
            elapsed = cooldownTicks;
        }

        long remaining = cooldownTicks - elapsed;
        return Math.max(remaining, 0);
    }

    public void updateCooldown(@NotNull UUID playerId, long currentTick) {
        playerCooldownMap.put(playerId, currentTick);
    }
}
