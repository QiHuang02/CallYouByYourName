package cn.qihuang02.callyou.api;

import cn.qihuang02.callyou.util.MentionKeyUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record MentionContext(
        ServerPlayer sender,
        Component originalMessage,
        String rawMessage,
        String mentionKey,
        @Nullable ResourceLocation typeId
) {
    public MentionContext(
            ServerPlayer sender,
            Component originalMessage,
            String rawMessage,
            String mentionKey
    ) {
        this(sender, originalMessage, rawMessage, mentionKey, null);
    }

    @Contract(pure = true)
    public @NotNull String mentionToken() {
        if (mentionKey == null || mentionKey.isEmpty()) {
            return "@";
        }
        return "@" + mentionKey;
    }

    @Contract(pure = true)
    public @NotNull String normalizedKey() {
        return MentionKeyUtils.normalize(mentionKey);
    }

    public boolean matchesKey(String key) {
        return MentionKeyUtils.matches(mentionKey, key);
    }

    public MinecraftServer server() {
        return sender.getServer();
    }

    public @NotNull ServerLevel level() {
        return sender.serverLevel();
    }

    public @NotNull ResourceKey<Level> dimension() {
        return sender.level().dimension();
    }

    public @NotNull UUID senderId() {
        return sender.getUUID();
    }

    public String senderName() {
        return sender.getGameProfile().getName();
    }

    public @NotNull Component senderDisplayName() {
        return sender.getDisplayName();
    }
}
