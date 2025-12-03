package cn.qihuang02.callyou.api;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public record MentionContext(
        ServerPlayer sender,
        Component originalMessage,
        String rawMessage,
        String mentionKey
) {
}
