package cn.qihuang02.callyou.core.client.screen.row;

import cn.qihuang02.callyou.compat.ftb.FTBChunksAPIWrapper;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class MentionHistoryRowUtils {
    static @Nullable String getTransientWaypointCommand(@NotNull Style style) {
        ClickEvent clickEvent = style.getClickEvent();
        if (clickEvent == null || clickEvent.getAction() != ClickEvent.Action.RUN_COMMAND) {
            return null;
        }
        String value = clickEvent.getValue();
        if (value == null || !value.startsWith(FTBChunksAPIWrapper.TRANSIENT_WAYPOINT_COMMAND)) {
            return null;
        }
        return value;
    }

    static @Nullable String getReplySuggestion(@NotNull Style style) {
        ClickEvent clickEvent = style.getClickEvent();
        if (clickEvent == null || clickEvent.getAction() != ClickEvent.Action.SUGGEST_COMMAND) {
            return null;
        }
        String value = clickEvent.getValue();
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }

    static @Nullable String normalizeSenderName(@Nullable String name) {
        if (name == null) {
            return null;
        }
        String trimmed = name.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
