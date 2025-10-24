package cn.qihuang02.cyyn.util;

import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record MentionParseResult(@NotNull List<ServerPlayer> players, boolean deniedGroupMention) {
}
