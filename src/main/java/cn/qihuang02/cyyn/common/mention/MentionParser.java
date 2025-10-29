package cn.qihuang02.cyyn.common.mention;

import cn.qihuang02.cyyn.api.mention.MentionAccessPolicy;
import cn.qihuang02.cyyn.api.mention.MentionGroup;
import cn.qihuang02.cyyn.api.mention.MentionGroupRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class MentionParser {
    public @NotNull MentionParseResult parse(@NotNull String message, @NotNull ServerPlayer sender) {
        Map<UUID, ServerPlayer> mentionedPlayers = new LinkedHashMap<>();
        PlayerList playerList = Objects.requireNonNull(sender.getServer()).getPlayerList();
        boolean deniedGroupMention = false;

        for (String word : message.split("\\s+")) {
            if (!word.startsWith("@") || word.length() <= 1) {
                continue;
            }

            String token = MentionTextUtils.normalizeToken(word.substring(1));
            if (token.isEmpty()) {
                continue;
            }

            Optional<MentionGroup> mentionGroup = MentionGroupRegistry.find(token);
            if (mentionGroup.isPresent()) {
                MentionGroup group = mentionGroup.get();
                MentionAccessPolicy accessPolicy = group.accessPolicy();
                if (accessPolicy.isDenied(sender)) {
                    deniedGroupMention = true;
                    continue;
                }
                group.resolveTargets(sender, playerList)
                        .forEach(player -> mentionedPlayers.putIfAbsent(player.getUUID(), player));

                continue;
            }

            ServerPlayer targetPlayer = playerList.getPlayerByName(token);
            if (targetPlayer != null && !targetPlayer.equals(sender)) {
                mentionedPlayers.putIfAbsent(targetPlayer.getUUID(), targetPlayer);
            }
        }

        return new MentionParseResult(new ArrayList<>(mentionedPlayers.values()), deniedGroupMention);
    }
}
