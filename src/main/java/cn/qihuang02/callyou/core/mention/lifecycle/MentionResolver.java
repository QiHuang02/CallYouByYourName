package cn.qihuang02.callyou.core.mention.lifecycle;

import cn.qihuang02.callyou.api.MentionCandidate;
import cn.qihuang02.callyou.api.MentionContext;
import cn.qihuang02.callyou.api.MentionType;
import cn.qihuang02.callyou.api.ResolveStatus;
import cn.qihuang02.callyou.api.components.TargetProvider;
import cn.qihuang02.callyou.core.mention.MentionTokens;
import cn.qihuang02.callyou.core.mention.components.targetProvider.NoneTargetProvider;
import cn.qihuang02.callyou.registry.CallYouMentionRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class MentionResolver implements MentionLifeCycle {
    private static final Map<Registry<MentionType>, Map<String, CachedType>> LOOKUP_CACHE = new ConcurrentHashMap<>();

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

    @Override
    public void process(@NotNull MentionContext context) {
        String rawText = context.rawText();
        if (rawText.isEmpty()) {
            return;
        }
        if (context.server() == null) {
            return;
        }

        var access = context.server().registryAccess();
        Optional<Registry<MentionType>> optionalRegistry =
                access.registry(CallYouMentionRegistries.MENTION_TYPE_REGISTRY_KEY);
        Registry<MentionType> registry = optionalRegistry.orElse(null);
        if (registry == null) {
            return;
        }
        CachedType playerMentionEntry = getLookup(registry).get("player");
        MentionType playerMentionType = playerMentionEntry != null ? playerMentionEntry.type : null;
        ResourceLocation playerMentionId = playerMentionEntry != null ? playerMentionEntry.id : null;

        for (MentionTokens.Token token : MentionTokens.scan(rawText)) {
            String key = token.key();
            int start = token.startIndex();
            int end = token.endIndex();
            if (start >= rawText.length() || end <= start) {
                continue;
            }

            String originalText = rawText.substring(start, Math.min(end, rawText.length()));
            MentionCandidate candidate = new MentionCandidate(start, end, originalText, key);

            MentionType type = null;
            ResourceLocation typeId = null;
            boolean resolvedTargets = false;

            if (registry != null) {
                String lowered = key.toLowerCase(Locale.ROOT);
                CachedType cached = getLookup(registry).get(lowered);
                if (cached != null) {
                    type = cached.type;
                    typeId = cached.id;
                }
            }

            if (type == null && playerMentionType != null) {
                candidate.setType(playerMentionType);
                candidate.setTypeId(playerMentionId);
                playerMentionType.targetProvider().resolveTargets(context, candidate);
                resolvedTargets = true;

                if (candidate.resolvedTargets().isEmpty()) {
                    candidate.setType(null);
                    candidate.setTypeId(null);
                    candidate.updateResolveStatus(ResolveStatus.NOT_FOUND_IGNORED, null);
                    context.addCandidate(candidate);
                    continue;
                }

                type = playerMentionType;
                typeId = playerMentionId;
            }

            if (type == null) {
                candidate.updateResolveStatus(ResolveStatus.NOT_FOUND_IGNORED, null);
                context.addCandidate(candidate);
                continue;
            }

            candidate.setType(type);
            candidate.setTypeId(typeId);

            TargetProvider targetProvider = type.targetProvider();
            if (!resolvedTargets) {
                targetProvider.resolveTargets(context, candidate);
            }

            if (candidate.resolveStatus() == ResolveStatus.PENDING && candidate.resolvedTargets().isEmpty()) {
                if (targetProvider instanceof NoneTargetProvider) {
                    candidate.updateResolveStatus(ResolveStatus.SUCCESS, null);
                } else {
                    ResolveStatus emptyStatus = typeId != null && typeId.equals(playerMentionId)
                            ? ResolveStatus.NOT_FOUND
                            : ResolveStatus.EMPTY_GROUP;
                    candidate.updateResolveStatus(emptyStatus, null);
                }
            }

            context.addCandidate(candidate);
        }
    }

    private record CachedType(MentionType type, ResourceLocation id) {
    }
}
