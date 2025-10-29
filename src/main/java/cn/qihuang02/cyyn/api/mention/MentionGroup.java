package cn.qihuang02.cyyn.api.mention;

import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Represents a reserved @-mention name that resolves to a dynamic player set.
 */
public interface MentionGroup extends Mention {
    /**
     * @return the canonical name (without the leading '@') that triggers this group mention.
     */
    @Override
    @NotNull
    String name();

    @Override
    default int permissionLevel() {
        return 2;
    }

    @Override
    default @NotNull ChatFormatting pointColor() {
        return ChatFormatting.LIGHT_PURPLE;
    }

    /**
     * Resolves the target players for this group mention. Third-party groups can override
     * {@link #permissionLevel()} to expose custom permission checks while reusing the parser logic.
     *
     * @param sender     the player who triggered the mention
     * @param playerList the server player list to search
     * @return the collection of players that should be notified
     */
    @NotNull
    Collection<ServerPlayer> resolveTargets(@NotNull ServerPlayer sender, @NotNull PlayerList playerList);
}
