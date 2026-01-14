package cn.qihuang02.callyou.core.mention.executor;

import cn.qihuang02.callyou.api.MentionType;
import cn.qihuang02.callyou.core.mention.components.formatter.ItemTextFormatter;
import org.jetbrains.annotations.NotNull;

final class MentionExecutionSupport {
    static boolean isItemMention(@NotNull MentionType type) {
        return type.textFormatter() instanceof ItemTextFormatter;
    }
}
