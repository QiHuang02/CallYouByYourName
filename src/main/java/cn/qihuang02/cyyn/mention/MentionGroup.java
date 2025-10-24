package cn.qihuang02.cyyn.mention;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Represents a reserved @-mention token that resolves to a dynamic player set.
 */
public interface MentionGroup {
    /**
     * @return the canonical token (without the leading '@') that triggers this group mention.
     */
    @NotNull
    String token();

    /**
     * @return {@code true} if the sender must have elevated permissions to use this group.
     */
    default boolean requiresPermission() {
        return true;
    }

    /**
     * Resolves the target players for this group mention.
     *
     * @param sender     the player who triggered the mention
     * @param playerList the server player list to search
     * @return the collection of players that should be notified
     */
    @NotNull
    Collection<ServerPlayer> resolveTargets(@NotNull ServerPlayer sender, @NotNull PlayerList playerList);
}
