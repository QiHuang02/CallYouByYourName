package cn.qihuang02.cyyn.util;

import cn.qihuang02.cyyn.mention.MentionGroup;
import cn.qihuang02.cyyn.mention.group.HereMentionGroup;
import cn.qihuang02.cyyn.mention.group.NearMentionGroup;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;

public final class MentionParser {
    private static final Map<String, MentionGroup> GROUP_MENTIONS = createGroupMentionRegistry();

    public static @NotNull MentionParseResult parse(@NotNull String message, @NotNull ServerPlayer sender) {
        Map<UUID, ServerPlayer> mentionedPlayers = new LinkedHashMap<>();
        PlayerList playerList = Objects.requireNonNull(sender.getServer()).getPlayerList();
        boolean deniedGroupMention = false;
        boolean hasGroupMentionPermission = sender.hasPermissions(2);

        for (String word : message.split("\\s+")) {
            if (!word.startsWith("@") || word.length() <= 1) {
                continue;
            }

            String token = normalizeMentionToken(word.substring(1));
            if (token.isEmpty()) {
                continue;
            }

            MentionGroup mentionGroup = GROUP_MENTIONS.get(token.toLowerCase(Locale.ROOT));
            if (mentionGroup != null) {
                if (mentionGroup.requiresPermission() && !hasGroupMentionPermission) {
                    deniedGroupMention = true;
                } else {
                    mentionGroup.resolveTargets(sender, playerList)
                            .forEach(player -> mentionedPlayers.putIfAbsent(player.getUUID(), player));
                }
                continue;
            }

            ServerPlayer targetPlayer = playerList.getPlayerByName(token);
            if (targetPlayer != null && !targetPlayer.equals(sender)) {
                mentionedPlayers.putIfAbsent(targetPlayer.getUUID(), targetPlayer);
            }
        }

        return new MentionParseResult(new ArrayList<>(mentionedPlayers.values()), deniedGroupMention);
    }

    private static @NotNull String normalizeMentionToken(@NotNull String rawToken) {
        int end = rawToken.length();
        while (end > 0) {
            char c = rawToken.charAt(end - 1);
            if (Character.isLetterOrDigit(c) || c == '_' || c == '-') {
                break;
            }
            end--;
        }
        return rawToken.substring(0, end);
    }

    private static @NotNull @UnmodifiableView Map<String, MentionGroup> createGroupMentionRegistry() {
        Map<String, MentionGroup> registry = new HashMap<>();
        registerMentionGroup(registry, new HereMentionGroup());
        registerMentionGroup(registry, new NearMentionGroup());
        return Collections.unmodifiableMap(registry);
    }

    private static void registerMentionGroup(@NotNull Map<String, MentionGroup> registry, MentionGroup mentionGroup) {
        registry.put(mentionGroup.token().toLowerCase(Locale.ROOT), mentionGroup);
    }
}
