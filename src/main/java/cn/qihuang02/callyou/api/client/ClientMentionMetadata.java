package cn.qihuang02.callyou.api.client;

import cn.qihuang02.callyou.CallYouByYourName;
import cn.qihuang02.callyou.api.MentionType;
import cn.qihuang02.callyou.api.components.InteractionDecorator;
import cn.qihuang02.callyou.core.mention.components.decorator.CompositeInteractionDecorator;
import cn.qihuang02.callyou.core.mention.components.decorator.TextColorDecorator;
import cn.qihuang02.callyou.core.mention.components.formatter.PlayerNameTextFormatter;
import cn.qihuang02.callyou.core.mention.components.formatter.SimpleTextFormatter;
import cn.qihuang02.callyou.registry.CallYouMentionRegistries;
import cn.qihuang02.callyou.util.MentionKeyUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

public record ClientMentionMetadata(
        @NotNull Set<String> mentionTypeKeys,
        @NotNull Map<String, Style> mentionTypeStyles
) {
    public ClientMentionMetadata {
        mentionTypeKeys = Set.copyOf(mentionTypeKeys);
        mentionTypeStyles = Collections.unmodifiableMap(new LinkedHashMap<>(mentionTypeStyles));
    }

    public static @NotNull ClientMentionMetadata create(@NotNull Minecraft minecraft) {
        LinkedHashSet<String> keys = new LinkedHashSet<>();
        LinkedHashMap<String, Style> styles = new LinkedHashMap<>();

        Map<ResourceLocation, MentionType> mentionTypes = CallYouMentionRegistries.mentionTypes();
        if (mentionTypes.isEmpty()) {
            return new ClientMentionMetadata(keys, styles);
        }

        for (Map.Entry<ResourceLocation, MentionType> entry : mentionTypes.entrySet()) {
            ResourceLocation id = entry.getKey();
            MentionType mentionType = entry.getValue();

            String simpleKey = id.getPath().toLowerCase(Locale.ROOT);

            keys.add(simpleKey);
            styles.put(simpleKey, resolveStyle(mentionType));
        }

        return new ClientMentionMetadata(keys, styles);
    }

    private static @NotNull Style resolveStyle(@NotNull MentionType mentionType) {
        if (mentionType.textFormatter() instanceof SimpleTextFormatter simple) {
            return resolveStyleFromDecorator(simple.decorator());
        }
        if (mentionType.textFormatter() instanceof PlayerNameTextFormatter player) {
            return resolveStyleFromDecorator(player.decorator());
        }
        return Style.EMPTY;
    }

    private static @NotNull Style resolveStyleFromDecorator(@NotNull InteractionDecorator decorator) {
        if (decorator instanceof TextColorDecorator textColor) {
            return Style.EMPTY.withColor(textColor.color());
        }
        if (decorator instanceof CompositeInteractionDecorator composite) {
            Style resolved = Style.EMPTY;
            for (InteractionDecorator entry : composite.decorators()) {
                if (entry instanceof TextColorDecorator textColor) {
                    resolved = resolved.withColor(textColor.color());
                }
            }
            return resolved;
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
        private static int lastMentionTypesSize = -1;

        public static @NotNull ClientMentionMetadata get(@NotNull Minecraft minecraft) {
            synchronized (LOCK) {
                int currentSize = CallYouMentionRegistries.mentionTypes().size();
                if (currentSize != lastMentionTypesSize) {
                    cached = create(minecraft);
                    lastMentionTypesSize = currentSize;
                }
                return cached;
            }
        }

        public static void invalidate() {
            synchronized (LOCK) {
                lastMentionTypesSize = -1;
            }
        }
    }
}
