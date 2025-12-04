package cn.qihuang02.callyou.core;

import cn.qihuang02.callyou.api.MentionRules;
import cn.qihuang02.callyou.api.MentionType;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class MentionGuard {
    public boolean canUseMentionType(@NotNull ServerPlayer sender, @NotNull MentionType type) {
        MentionRules rules = type.rules();
        if (rules == null) {
            return true;
        }

        int minOp = rules.minOpLevel();
        if (minOp > 0 && !sender.hasPermissions(minOp)) {
            return false;
        }

//        String permission = rules.permission();
//        if (permission != null && !permission.isEmpty()) {
//
//        }
        return true;
    }
}
