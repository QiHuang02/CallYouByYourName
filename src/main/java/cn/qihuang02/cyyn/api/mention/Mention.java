package cn.qihuang02.cyyn.api.mention;

import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Common contract for all @mention concepts.
 */
public interface Mention {
    /**
     * @return the canonical name (without the leading '@') associated with the mention.
     */
    @NotNull
    String name();

    /**
     * @return the minimum permission level required to invoke this mention.
     */
    int permissionLevel();

    /**
     * @return the highlight color used when rendering mentions of this type.
     */
    @NotNull
    ChatFormatting pointColor();

    /**
     * @return {@code true} if the sender meets the required permission level for this mention.
     */
    default boolean isAllowed(@NotNull ServerPlayer sender) {
        return sender.hasPermissions(Math.max(0, permissionLevel()));
    }

    /**
     * @return {@code true} if the sender does not meet the required permission level for this mention.
     */
    default boolean isDenied(@NotNull ServerPlayer sender) {
        return !isAllowed(sender);
    }
}
