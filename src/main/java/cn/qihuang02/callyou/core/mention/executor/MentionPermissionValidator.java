package cn.qihuang02.callyou.core.mention.executor;

import cn.qihuang02.callyou.api.MentionType;
import cn.qihuang02.callyou.core.MentionGuard;
import cn.qihuang02.callyou.core.MentionResolver;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class MentionPermissionValidator {
    private final MentionGuard guard;

    public MentionPermissionValidator(@NotNull MentionGuard guard) {
        this.guard = guard;
    }

    public @NotNull ValidationResult validate(
            @NotNull ServerPlayer sender,
            @NotNull List<MentionResolver.ResolvedMention> mentions
    ) {
        int effectiveMentionCount = 0;
        boolean itemMentionUsed = false;

        for (MentionResolver.ResolvedMention parsed : mentions) {
            MentionType type = parsed.mentionType();
            if (type == null) {
                continue;
            }
            if (MentionExecutionSupport.isItemMention(type)) {
                if (itemMentionUsed) {
                    continue;
                }
                itemMentionUsed = true;
            }
            effectiveMentionCount++;

            if (!guard.canUseMentionType(sender, type, parsed.typeId())) {
                String key = parsed.key();
                Component display = Component.literal("@" + key);
                Component error = Component.translatable("message.callyou.no_permission", display);
                return new ValidationResult(effectiveMentionCount, error);
            }
        }

        return new ValidationResult(effectiveMentionCount, null);
    }

    public record ValidationResult(int effectiveMentionCount, @Nullable Component errorMessage) {
    }
}
