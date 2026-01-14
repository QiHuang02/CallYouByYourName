package cn.qihuang02.callyou.core;

import cn.qihuang02.callyou.api.MentionType;
import cn.qihuang02.callyou.core.handler.OnlinePlayersHandler;
import cn.qihuang02.callyou.registry.CallYouMentionRegistries;
import cn.qihuang02.callyou.util.OfflinePlayerList;
import cn.qihuang02.callyou.util.OnlinePlayerList;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class MentionResolver {
    private static final Map<Registry<MentionType>, Map<String, CachedType>> LOOKUP_CACHE = new ConcurrentHashMap<>();

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

        Map<String, CachedType> lookup = registry != null ? getLookup(registry) : Map.of();
        CachedType playerMentionEntry = lookup.get("player");
        MentionType playerMentionType = playerMentionEntry != null ? playerMentionEntry.type : null;
        ResourceLocation playerMentionId = playerMentionEntry != null ? playerMentionEntry.id : null;

        OnlinePlayerList onlinePlayers = OnlinePlayersHandler.getOnlinePlayers();
        OfflinePlayerList offlinePlayers = OnlinePlayersHandler.getOfflinePlayers();

        for (MentionTokens.Token token : MentionTokens.scan(rawText)) {
            String key = token.key();
            int start = token.startIndex();
            int end = token.endIndex();

            MentionType type = null;
            ResourceLocation typeId = null;
            ServerPlayer playerTarget = null;

            if (!lookup.isEmpty()) {
                String lowered = key.toLowerCase(Locale.ROOT);
                CachedType cached = lookup.get(lowered);
                if (cached != null) {
                    type = cached.type;
                    typeId = cached.id;
                }
            }

            if (type == null) {
                UUID targetID = onlinePlayers.findPlayerByExactName(server, key);
                if (targetID != null) {
                    ServerPlayer candidate = server.getPlayerList().getPlayer(targetID);
                    if (candidate != null) {
                        playerTarget = candidate;
                        type = playerMentionType;
                        typeId = playerMentionId;
                    }
                }
            }

            if (type == null && playerMentionType != null) {
                UUID targetID = offlinePlayers.findPlayerByExactName(server, key);
                if (targetID != null) {
                    type = playerMentionType;
                    typeId = playerMentionId;
                }
            }

            result.add(new ResolvedMention(start, end, key, type, typeId, playerTarget));
        }

        return result;
    }

    private static @NotNull Map<String, CachedType> getLookup(@NotNull Registry<MentionType> registry) {
        Map<String, CachedType> cached = LOOKUP_CACHE.get(registry);
        if (cached != null && cached.size() == registry.size()) {
            return cached;
        }

        Map<String, CachedType> map = new HashMap<>();
        for (var entry : registry.entrySet()) {
            var id = registry.getKey(entry.getValue());
            if (id != null) {
                map.put(id.getPath().toLowerCase(Locale.ROOT), new CachedType(entry.getValue(), id));
            }
        }

        LOOKUP_CACHE.put(registry, map);
        return map;
    }

    private record CachedType(MentionType type, ResourceLocation id) {
    }

    public record ResolvedMention(
            int startIndex,
            int endIndex,
            @NotNull String key,
            @Nullable MentionType mentionType,
            @Nullable ResourceLocation typeId,
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
