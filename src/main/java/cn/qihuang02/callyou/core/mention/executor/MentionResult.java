package cn.qihuang02.callyou.core.mention.executor;

import cn.qihuang02.callyou.core.MentionResolver;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class MentionResult {
    public enum MentionStatus {
        ALLOWED(true, false, false),
        TARGETLESS_ALLOWED(true, false, false),
        BLOCKED_PREFS(false, true, true),
        BLOCKED_BLACKLIST(false, true, true),
        BLOCKED_TYPE(false, true, true),
        BLOCKED_MASS(false, true, true),
        NO_TARGETS(false, true, false),
        NO_PERMISSION(false, true, false),
        NOT_FOUND(false, false, false);

        private final boolean allowed;
        private final boolean countsAsFailure;
        private final boolean collectBlockedName;

        MentionStatus(boolean allowed, boolean countsAsFailure, boolean collectBlockedName) {
            this.allowed = allowed;
            this.countsAsFailure = countsAsFailure;
            this.collectBlockedName = collectBlockedName;
        }

        public boolean isAllowed() {
            return allowed;
        }

        public boolean countsAsFailure() {
            return countsAsFailure;
        }

        public boolean shouldCollectBlockedName() {
            return collectBlockedName;
        }
    }

    public enum MessageMentionOutcome {
        ALLOW_ALL,
        STRIP_SOME,
        CANCEL_ALL
    }

    public record MentionEntry(
            @NotNull MentionResolver.ResolvedMention mention,
            @NotNull MentionStatus status
    ) {
        public @NotNull MentionEntry withStatus(@NotNull MentionStatus updated) {
            return new MentionEntry(mention, updated);
        }
    }

    public record MessageResult(
            @NotNull List<MentionEntry> entries,
            @NotNull MessageMentionOutcome outcome,
            @Nullable Component failureMessage
    ) {
        public @NotNull List<MentionResolver.ResolvedMention> allowedMentions() {
            List<MentionResolver.ResolvedMention> allowed = new ArrayList<>();
            for (MentionEntry entry : entries) {
                if (entry.status().isAllowed()) {
                    allowed.add(entry.mention());
                }
            }
            return allowed;
        }
    }

    public record PermissionResult(
            @NotNull List<MentionEntry> entries,
            int effectiveMentionCount,
            @Nullable Component errorMessage
    ) {
    }
}
