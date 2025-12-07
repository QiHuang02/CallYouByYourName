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
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class ClientMentionContext {
    private final Set<String> mentionTypeKeys;

    public ClientMentionContext(@NotNull Set<String> keys, @NotNull Map<String, Style> styles) {
        this.mentionTypeKeys = Set.copyOf(keys);
    }

    public static @NotNull ClientMentionContext create(@NotNull Minecraft minecraft) {
        var keys = new LinkedHashSet<String>();
        var styles = new LinkedHashMap<String, Style>();

        if (minecraft.level == null) {
            return new ClientMentionContext(keys, styles);
        }

        var registryAccess = minecraft.level.registryAccess();
        var optRegistry = registryAccess.registry(CallYouMentionRegistries.MENTION_TYPE_REGISTRY_KEY);
        if (optRegistry.isEmpty()) {
            CallYouByYourName.LOGGER.warn("[CallYou] Mention type registry is not available on the client.");
            return new ClientMentionContext(keys, styles);
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

        return new ClientMentionContext(keys, styles);
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
}
