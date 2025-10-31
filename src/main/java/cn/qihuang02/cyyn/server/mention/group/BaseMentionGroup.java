package cn.qihuang02.cyyn.server.mention.group;

import cn.qihuang02.cyyn.api.mention.MentionGroup;
import cn.qihuang02.cyyn.common.config.Config;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.stream.Collectors;

public abstract class BaseMentionGroup implements MentionGroup {
    @Override
    public @NotNull Collection<ServerPlayer> resolveTargets(@NotNull ServerPlayer sender, @NotNull PlayerList playerList) {
        ServerLevel senderLevel = sender.serverLevel();
        return playerList.getPlayers().stream()
                .filter(player -> !player.equals(sender))
                .filter(player -> player.serverLevel().dimension().equals(senderLevel.dimension()))
                .filter(player -> shouldInclude(sender, player))
                .collect(Collectors.toList());
    }

    /**
     * Determines whether a potential target should be included in this mention group.
     *
     * @param sender the player issuing the mention
     * @param target the player being evaluated
     * @return {@code true} if the player should be included
     */
    protected boolean shouldInclude(@NotNull ServerPlayer sender, @NotNull ServerPlayer target) {
        return true;
    }

    @Override
    public int coolDown() {
        return Config.mentionCooldownTicks;
    }
}
