package cn.qihuang02.callyou.core.mention.executor;

import cn.qihuang02.callyou.api.MentionContext;
import cn.qihuang02.callyou.api.MentionType;
import cn.qihuang02.callyou.core.mention.executor.MentionResult.MentionStatus;
import org.jetbrains.annotations.NotNull;

public record MentionExecution(
        @NotNull MentionType type,
        @NotNull MentionContext context,
        @NotNull MentionStatus status
) {
}
