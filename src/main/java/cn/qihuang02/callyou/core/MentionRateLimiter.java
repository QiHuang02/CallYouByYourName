package cn.qihuang02.callyou.core;

import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class MentionRateLimiter {

    private final Map<UUID, Long> lastMessageTick = new HashMap<>();
    private final Map<UUID, Map<UUID, Long>> lastSenderToTargetTick = new HashMap<>();

    public boolean canSendMessage(ServerPlayer sender, long nowTick, int cooldownTicks) {
        if (cooldownTicks <= 0) {
            return true;
        }
        UUID id = sender.getUUID();
        Long last = lastMessageTick.get(id);
        if (last == null) {
            return true;
        }
        return nowTick - last >= cooldownTicks;
    }

    public boolean canMentionTarget(
            ServerPlayer sender,
            ServerPlayer target,
            long nowTick,
            int cooldownTicks
    ) {
        if (cooldownTicks <= 0) {
            return true;
        }
        UUID senderId = sender.getUUID();
        UUID targetId = target.getUUID();

        Map<UUID, Long> map = lastSenderToTargetTick.get(senderId);
        if (map == null) {
            return true;
        }
        Long last = map.get(targetId);
        if (last == null) {
            return true;
        }
        return nowTick - last >= cooldownTicks;
    }

    public void record(@NotNull ServerPlayer sender, @NotNull Iterable<ServerPlayer> targets, long nowTick) {
        UUID senderId = sender.getUUID();
        lastMessageTick.put(senderId, nowTick);

        Map<UUID, Long> map = lastSenderToTargetTick.computeIfAbsent(senderId, k -> new HashMap<>());
        for (ServerPlayer target : targets) {
            map.put(target.getUUID(), nowTick);
        }
    }
}
