package cn.qihuang02.cyyn.mention;

import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Encapsulates the permission requirements for invoking a mention group.
 */
public interface MentionAccessPolicy {
    /**
     * @return an access policy that allows all senders to use the group.
     */
    static @NotNull MentionAccessPolicy alwaysAllow() {
        return sender -> true;
    }

    /**
     * @return an access policy that requires the sender to have at least the given permission level.
     */
    static @NotNull MentionAccessPolicy requiresPermissionLevel(int permissionLevel) {
        return sender -> sender.hasPermissions(permissionLevel);
    }

    /**
     * @return {@code true} if the sender is allowed to use the associated mention group.
     */
    boolean isAllowed(@NotNull ServerPlayer sender);

    /**
     * @return {@code true} if the sender is not allowed to use the mention group.
     */
    default boolean isDenied(@NotNull ServerPlayer sender) {
        return !isAllowed(sender);
    }
}
