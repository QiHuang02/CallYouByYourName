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
     * @return the access policy that determines whether the sender may invoke this group.
     */
    default @NotNull MentionAccessPolicy accessPolicy() {
        return MentionAccessPolicy.requiresPermissionLevel(2);
    }

    /**
     * Resolves the target players for this group mention. Third-party groups can override
     * {@link #accessPolicy()} to expose custom permission checks while reusing the parser logic.
     *
     * @param sender     the player who triggered the mention
     * @param playerList the server player list to search
     * @return the collection of players that should be notified
     */
    @NotNull
    Collection<ServerPlayer> resolveTargets(@NotNull ServerPlayer sender, @NotNull PlayerList playerList);
}
