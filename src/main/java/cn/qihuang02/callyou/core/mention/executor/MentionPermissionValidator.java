package cn.qihuang02.callyou.core.mention.executor;

import cn.qihuang02.callyou.api.MentionType;
import cn.qihuang02.callyou.core.MentionGuard;
import cn.qihuang02.callyou.core.mention.components.formatter.ItemTextFormatter;
import cn.qihuang02.callyou.core.mention.executor.MentionResult.MentionEntry;
import cn.qihuang02.callyou.core.mention.executor.MentionResult.MentionStatus;
import cn.qihuang02.callyou.core.mention.executor.MentionResult.PermissionResult;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class MentionPermissionValidator {
    private final MentionGuard guard;

    public MentionPermissionValidator(@NotNull MentionGuard guard) {
        this.guard = guard;
    }

    public @NotNull PermissionResult applyPermissions(
            @NotNull ServerPlayer sender,
            @NotNull List<MentionEntry> entries
    ) {
        int effectiveMentionCount = 0;
        boolean itemMentionUsed = false;
        List<MentionEntry> updatedEntries = new ArrayList<>(entries.size());

        for (MentionEntry entry : entries) {
            MentionStatus status = entry.status();
            MentionType type = entry.mention().mentionType();
            if (!status.isAllowed() || type == null) {
                updatedEntries.add(entry);
                continue;
            }
            if (isItemMention(type)) {
                if (itemMentionUsed) {
                    updatedEntries.add(entry);
                    continue;
                }
                itemMentionUsed = true;
            }
            effectiveMentionCount++;

            if (!guard.canUseMentionType(sender, type, entry.mention().typeId())) {
                String key = entry.mention().key();
                Component display = Component.literal("@" + key);
                Component error = Component.translatable("message.callyou.no_permission", display);
                updatedEntries.add(entry.withStatus(MentionStatus.NO_PERMISSION));
                return new PermissionResult(updatedEntries, effectiveMentionCount, error);
            }
            updatedEntries.add(entry);
        }

        return new PermissionResult(updatedEntries, effectiveMentionCount, null);
    }

    private boolean isItemMention(@NotNull MentionType type) {
        return type.textFormatter() instanceof ItemTextFormatter;
    }

}
