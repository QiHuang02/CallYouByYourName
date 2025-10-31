package cn.qihuang02.cyyn.server.mention.group;

import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Resolves the {@code @near} mention to nearby players in the sender's dimension.
 */
public class NearMentionGroup extends BaseMentionGroup {
    private static final double NEAR_MENTION_RANGE_SQ = 32D * 32D;

    @Override
    public @NotNull String name() {
        return "near";
    }

    @Override
    public int permissionLevel() {
        return 0;
    }

    @Override
    protected boolean shouldInclude(@NotNull ServerPlayer sender, @NotNull ServerPlayer target) {
        return sender.distanceToSqr(target) <= NEAR_MENTION_RANGE_SQ;
    }

    @Override
    public int coolDown() {
        return super.coolDown();
    }
}
