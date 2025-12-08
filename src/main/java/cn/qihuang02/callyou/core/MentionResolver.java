package cn.qihuang02.callyou.core;

import cn.qihuang02.callyou.api.MentionType;
import cn.qihuang02.callyou.event.OnlinePlayersHandler;
import cn.qihuang02.callyou.registry.CallYouMentionRegistries;
import cn.qihuang02.callyou.util.OnlinePlayerList;
import net.minecraft.core.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class MentionResolver {
    private static final Map<Registry<MentionType>, Map<String, MentionType>> LOOKUP_CACHE = new ConcurrentHashMap<>();

    public static @NotNull List<ResolvedMention> resolve(
            @NotNull MinecraftServer server,
            @NotNull ServerPlayer sender,
            @NotNull String rawText
    ) {
        List<ResolvedMention> result = new ArrayList<>();

        var access = server.registryAccess();
        Optional<Registry<MentionType>> optionalRegistry =
                access.registry(CallYouMentionRegistries.MENTION_TYPE_REGISTRY_KEY);
        Registry<MentionType> registry = optionalRegistry.orElse(null);

        MentionType playerMentionType = registry != null ? getLookup(registry).get("player") : null;

        OnlinePlayerList onlinePlayers = OnlinePlayersHandler.getOnlinePlayers();

        for (MentionTokens.Token token : MentionTokens.scan(rawText)) {
            String key = token.key();
            int start = token.startIndex();
            int end = token.endIndex();

            MentionType type = null;
            ServerPlayer playerTarget = null;

            if (registry != null) {
                String lowered = key.toLowerCase(Locale.ROOT);
                type = getLookup(registry).get(lowered);
            }

            if (type == null) {
                UUID targetID = onlinePlayers.findOnlinePlayerByExactName(server, key);
                if (targetID != null) {
                    ServerPlayer candidate = server.getPlayerList().getPlayer(targetID);
                    if (candidate != null) {
                        playerTarget = candidate;
                        type = playerMentionType;
                    }
                }
            }

            result.add(new ResolvedMention(start, end, key, type, playerTarget));
        }

        return result;
    }

    private static @NotNull Map<String, MentionType> getLookup(@NotNull Registry<MentionType> registry) {
        Map<String, MentionType> cached = LOOKUP_CACHE.get(registry);
        if (cached != null && cached.size() == registry.size()) {
            return cached;
        }

        Map<String, MentionType> map = new HashMap<>();
        for (var entry : registry.entrySet()) {
            var id = registry.getKey(entry.getValue());
            if (id != null) {
                map.put(id.getPath().toLowerCase(Locale.ROOT), entry.getValue());
            }
        }

        LOOKUP_CACHE.put(registry, map);
        return map;
    }

    public record ResolvedMention(
            int startIndex,
            int endIndex,
            @NotNull String key,
            @Nullable MentionType mentionType,
            @Nullable ServerPlayer playerTarget
    ) {
        public boolean isPlayerMention() {
            return playerTarget != null;
        }

        public boolean isTypeMention() {
            return mentionType != null && playerTarget == null;
        }
    }
}
