package cn.qihuang02.callyou.chat;

import cn.qihuang02.callyou.CallYouByYourName;
import cn.qihuang02.callyou.api.MentionType;
import cn.qihuang02.callyou.registry.CallYouMentionRegistries;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class ClientMentionContext {
    private final Minecraft minecraft;

    private Set<String> mentionTypeKeys = Set.of();

    private Map<String, Style> mentionTypeStyles = Map.of();

    public ClientMentionContext(@NotNull Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    public void rebuild() {
        var connection = minecraft.getConnection();
        if (connection == null) {
            this.mentionTypeKeys = Set.of();
            this.mentionTypeStyles = Map.of();
            return;
        }

        var access = connection.registryAccess();
        Optional<Registry<MentionType>> optionalRegistry =
                access.registry(CallYouMentionRegistries.MENTION_TYPE_REGISTRY_KEY);

        if (optionalRegistry.isEmpty()) {
            this.mentionTypeKeys = Set.of();
            this.mentionTypeStyles = Map.of();
            return;
        }

        Registry<MentionType> registry = optionalRegistry.get();

        Set<String> keys = new LinkedHashSet<>();
        Map<String, Style> styles = new LinkedHashMap<>();

        for (var entry : registry.entrySet()) {
            ResourceLocation id = registry.getKey(entry.getValue());
            if (id == null) continue;

            if (CallYouByYourName.MODID.equals(id.getNamespace())
                    && "player".equals(id.getPath())) {
                continue;
            }

            String key = id.getPath();
            String normalized = key.toLowerCase(Locale.ROOT);
            keys.add(normalized);

            styles.put(normalized, Style.EMPTY.withColor(ChatFormatting.LIGHT_PURPLE));
        }

        this.mentionTypeKeys = Collections.unmodifiableSet(keys);
        this.mentionTypeStyles = Collections.unmodifiableMap(styles);
    }

    public @NotNull Set<String> getMentionTypeKeys() {
        return mentionTypeKeys;
    }

    public @NotNull Map<String, Style> getMentionTypeStyles() {
        return mentionTypeStyles;
    }
}
