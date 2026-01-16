package cn.qihuang02.callyou.api.client;

import cn.qihuang02.callyou.util.OnlinePlayerList;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public record ClientMentionContext(
        @NotNull ClientMentionMetadata metadata,
        @NotNull List<String> onlinePlayerNames
) {
    public ClientMentionContext {
        onlinePlayerNames = List.copyOf(onlinePlayerNames);
    }

    public static @NotNull ClientMentionContext get(@NotNull Minecraft minecraft) {
        return new ClientMentionContext(
                ClientMentionMetadata.ClientMentionMetadataCache.get(minecraft),
                OnlinePlayerList.getClientOnlinePlayerNames(minecraft)
        );
    }

    public @NotNull List<String> getMentionTypeKeysSorted() {
        return metadata.getMentionTypeKeysSorted();
    }

    public boolean hasMentionType(@NotNull String key) {
        return metadata.hasMentionType(key);
    }

    public @NotNull Optional<Style> findStyle(@NotNull String key) {
        return metadata.findStyle(key);
    }

    public @NotNull Style getStyleOrEmpty(@NotNull String key) {
        return metadata.getStyleOrEmpty(key);
    }

    public @NotNull List<String> getOnlinePlayerNames() {
        return onlinePlayerNames;
    }
}
