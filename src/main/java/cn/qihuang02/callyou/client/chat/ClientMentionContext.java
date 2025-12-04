package cn.qihuang02.callyou.client.chat;

import cn.qihuang02.callyou.CallYouByYourName;
import cn.qihuang02.callyou.api.MentionType;
import cn.qihuang02.callyou.core.impl.formatter.PlayerNameTextFormatter;
import cn.qihuang02.callyou.core.impl.formatter.SimpleTextFormatter;
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

    private Style playerMentionStyle = Style.EMPTY;

    public ClientMentionContext(@NotNull Minecraft minecraft) {
        this.minecraft = minecraft;
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

    public void rebuild() {
        var connection = minecraft.getConnection();
        if (connection == null) {
            this.mentionTypeKeys = Set.of();
            this.mentionTypeStyles = Map.of();
            this.playerMentionStyle = Style.EMPTY;
            return;
        }

        var access = connection.registryAccess();
        Optional<Registry<MentionType>> optionalRegistry =
                access.registry(CallYouMentionRegistries.MENTION_TYPE_REGISTRY_KEY);

        if (optionalRegistry.isEmpty()) {
            this.mentionTypeKeys = Set.of();
            this.mentionTypeStyles = Map.of();
            this.playerMentionStyle = Style.EMPTY;
            return;
        }

        Registry<MentionType> registry = optionalRegistry.get();

        Set<String> keys = new LinkedHashSet<>();
        Map<String, Style> styles = new LinkedHashMap<>();
        Style playerStyle = Style.EMPTY;

        for (var entry : registry.entrySet()) {
            MentionType mentionType = entry.getValue();
            ResourceLocation id = registry.getKey(mentionType);
            if (id == null) {
                continue;
            }

            String path = id.getPath();
            if (path == null || path.isEmpty()) {
                continue;
            }

            String normalized = path.toLowerCase(Locale.ROOT);
            Style style = resolveStyle(mentionType);

            if (CallYouByYourName.MODID.equals(id.getNamespace()) && "player".equals(id.getPath())) {
                playerStyle = style;
                continue;
            }

            keys.add(normalized);
            styles.put(normalized, style);
        }

        this.mentionTypeKeys = Collections.unmodifiableSet(keys);
        this.mentionTypeStyles = Collections.unmodifiableMap(styles);
        this.playerMentionStyle = playerStyle;
    }

    public @NotNull Set<String> getMentionTypeKeys() {
        return mentionTypeKeys;
    }

    public @NotNull Map<String, Style> getMentionTypeStyles() {
        return mentionTypeStyles;
    }

    public @NotNull Style getPlayerMentionStyle() {
        return playerMentionStyle == null ? Style.EMPTY : playerMentionStyle;
    }
}
