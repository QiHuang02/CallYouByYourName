package cn.qihuang02.callyou.core.impl.target;

import cn.qihuang02.callyou.api.event.MentionEvent;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class PlayerMentionHelper {
    public static boolean isPlayerMention(@NotNull MentionEvent event) {
        return event.getMentionType().targetProvider() instanceof PlayerNameTargetProvider;
    }

    public static @NotNull List<String> getMatchedPlayerNames(@NotNull MentionEvent event) {
        List<ServerPlayer> targets = event.getTargets();
        List<String> names = new ArrayList<>(targets.size());
        for (ServerPlayer player : targets) {
            names.add(player.getGameProfile().getName());
        }
        return names;
    }

    public static @Nullable String getSingleMatchedPlayerName(@NotNull MentionEvent event) {
        if (!isPlayerMention(event)) {
            return null;
        }
        if (!event.isSingleTarget()) {
            return null;
        }
        return event.getSingleTargetPlayerName();
    }
}
