package cn.qihuang02.callyou.core;

import cn.qihuang02.callyou.CallYouByYourName;
import cn.qihuang02.callyou.api.MentionType;
import cn.qihuang02.callyou.registry.CallYouMentionRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class MentionResolver {
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

    public static @NotNull List<ResolvedMention> resolve(@NotNull MinecraftServer server,
                                                         @NotNull ServerPlayer sender,
                                                         @NotNull String rawText) {
        List<ResolvedMention> result = new ArrayList<>();

        var access = server.registryAccess();
        Optional<Registry<MentionType>> optionalRegistry =
                access.registry(CallYouMentionRegistries.MENTION_TYPE_REGISTRY_KEY);
        Registry<MentionType> registry = optionalRegistry.orElse(null);

        MentionType playerMentionType = null;
        if (registry != null) {
            playerMentionType = registry.get(ResourceLocation.fromNamespaceAndPath(CallYouByYourName.MODID, "player"));
        }

        for (MentionTokens.Token token : MentionTokens.scan(rawText)) {
            String key = token.key();
            int start = token.startIndex();
            int end = token.endIndex();

            MentionType type = null;
            ServerPlayer playerTarget = null;

            if (registry != null) {
                String lowered = key.toLowerCase(Locale.ROOT);
                for (var entry : registry.entrySet()) {
                    ResourceLocation id = registry.getKey(entry.getValue());
                    if (id != null && id.getPath().equals(lowered)) {
                        type = entry.getValue();
                        break;
                    }
                }
            }

            if (type == null) {
                ServerPlayer candidate = server.getPlayerList().getPlayerByName(key);
                if (candidate != null) {
                    playerTarget = candidate;
                    type = playerMentionType;
                }
            }

            result.add(new ResolvedMention(start, end, key, type, playerTarget));
        }

        return result;
    }
}
