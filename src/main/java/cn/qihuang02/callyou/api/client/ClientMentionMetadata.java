package cn.qihuang02.callyou.api.client;

import cn.qihuang02.callyou.CallYouByYourName;
import cn.qihuang02.callyou.api.MentionType;
import cn.qihuang02.callyou.util.MentionKeyUtils;
import cn.qihuang02.callyou.core.mention.components.formatter.PlayerNameTextFormatter;
import cn.qihuang02.callyou.core.mention.components.formatter.SimpleTextFormatter;
import cn.qihuang02.callyou.registry.CallYouMentionRegistries;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public record ClientMentionMetadata(
        @NotNull Set<String> mentionTypeKeys,
        @NotNull Map<String, Style> mentionTypeStyles
) {
    public ClientMentionMetadata {
        mentionTypeKeys = Set.copyOf(mentionTypeKeys);
        mentionTypeStyles = Collections.unmodifiableMap(new LinkedHashMap<>(mentionTypeStyles));
    }

    public static @NotNull ClientMentionMetadata create(@NotNull Minecraft minecraft) {
        var keys = new LinkedHashSet<String>();
        var styles = new LinkedHashMap<String, Style>();

        if (minecraft.level == null) {
            return new ClientMentionMetadata(keys, styles);
        }

        var registryAccess = minecraft.level.registryAccess();
        var optRegistry = registryAccess.registry(CallYouMentionRegistries.MENTION_TYPE_REGISTRY_KEY);
        if (optRegistry.isEmpty()) {
            CallYouByYourName.LOGGER.warn("[CallYou] Mention type registry is not available on the client.");
            return new ClientMentionMetadata(keys, styles);
        }

        Registry<MentionType> registry = optRegistry.get();

        for (Map.Entry<ResourceKey<MentionType>, MentionType> entry : registry.entrySet()) {
            ResourceKey<MentionType> key = entry.getKey();
            MentionType mentionType = entry.getValue();

            ResourceLocation id = key.location();
            String simpleKey = id.getPath().toLowerCase(Locale.ROOT);

            keys.add(simpleKey);
            styles.put(simpleKey, resolveStyle(mentionType));
        }

        return new ClientMentionMetadata(keys, styles);
    }

    private static @NotNull Style resolveStyle(@NotNull MentionType mentionType) {
        if (mentionType.textFormatter() instanceof SimpleTextFormatter simple) {
            return Style.EMPTY.withColor(simple.color());
        }
        if (mentionType.textFormatter() instanceof PlayerNameTextFormatter(ChatFormatting color)) {
            return Style.EMPTY.withColor(color);
        }
        return Style.EMPTY;
    }

    public @NotNull Set<String> getMentionTypeKeys() {
        return mentionTypeKeys;
    }

    public @NotNull @Unmodifiable List<String> getMentionTypeKeysSorted() {
        List<String> result = new ArrayList<>(mentionTypeKeys);
        result.sort(String.CASE_INSENSITIVE_ORDER);
        return List.copyOf(result);
    }

    public @NotNull Map<String, Style> getMentionTypeStyles() {
        return mentionTypeStyles;
    }

    public boolean hasMentionType(@NotNull String key) {
        if (key.isEmpty()) {
            return false;
        }
        return mentionTypeStyles.containsKey(MentionKeyUtils.normalize(key));
    }

    public @NotNull Optional<Style> findStyle(@NotNull String key) {
        if (key.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(mentionTypeStyles.get(MentionKeyUtils.normalize(key)));
    }

    public @NotNull Style getStyleOrEmpty(@NotNull String key) {
        if (key.isEmpty()) {
            return Style.EMPTY;
        }
        return mentionTypeStyles.getOrDefault(MentionKeyUtils.normalize(key), Style.EMPTY);
    }

    public static final class ClientMentionMetadataCache {
        private static final Object LOCK = new Object();
        private static ClientMentionMetadata cached = new ClientMentionMetadata(Set.of(), Map.of());
        private static Object lastLevel;
        private static Object lastRegistryAccess;

        public static @NotNull ClientMentionMetadata get(@NotNull Minecraft minecraft) {
            synchronized (LOCK) {
                if (minecraft.level == null) {
                    cached = new ClientMentionMetadata(Set.of(), Map.of());
                    lastLevel = null;
                    lastRegistryAccess = null;
                    return cached;
                }
                Object level = minecraft.level;
                Object registryAccess = minecraft.level.registryAccess();
                if (level != lastLevel || registryAccess != lastRegistryAccess) {
                    cached = create(minecraft);
                    lastLevel = level;
                    lastRegistryAccess = registryAccess;
                }
                return cached;
            }
        }

        public static void invalidate() {
            synchronized (LOCK) {
                lastLevel = null;
                lastRegistryAccess = null;
            }
        }
    }
}
