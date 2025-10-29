package cn.qihuang02.cyyn.server.mention.group;

import org.jetbrains.annotations.NotNull;

/**
 * Resolves the {@code @here} mention to all online players in the sender's dimension.
 */
public class HereMentionGroup extends BaseMentionGroup {
    @Override
    public @NotNull String name() {
        return "here";
    }

    @Override
    public int permissionLevel() {
        return 0;
    }
}
