package cn.qihuang02.cyyn.mention.group;

import cn.qihuang02.cyyn.mention.MentionGroup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Resolves the {@code @near} mention to nearby players in the sender's dimension.
 */
public class NearMentionGroup implements MentionGroup {
    private static final double NEAR_MENTION_RANGE_SQ = 32D * 32D;

    @Override
    public @NotNull String token() {
        return "near";
    }

    @Override
    public @NotNull Collection<ServerPlayer> resolveTargets(@NotNull ServerPlayer sender, @NotNull PlayerList playerList) {
        ServerLevel senderLevel = sender.serverLevel();
        return playerList.getPlayers().stream()
                .filter(player -> !player.equals(sender))
                .filter(player -> player.serverLevel().dimension().equals(senderLevel.dimension()))
                .filter(player -> sender.distanceToSqr(player) <= NEAR_MENTION_RANGE_SQ)
                .collect(Collectors.toList());
    }
}
