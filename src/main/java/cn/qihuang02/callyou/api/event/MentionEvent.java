package cn.qihuang02.callyou.api.event;

import cn.qihuang02.callyou.api.MentionContext;
import cn.qihuang02.callyou.api.MentionType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class MentionEvent extends Event {
    private final MentionContext context;
    private final MentionType mentionType;
    private final List<ServerPlayer> targets;

    protected MentionEvent(@NotNull MentionContext context, @NotNull MentionType mentionType, @NotNull List<ServerPlayer> targets) {
        this.context = context;
        this.mentionType = mentionType;
        this.targets = new ArrayList<>(targets);
    }

    public @NotNull MentionContext getContext() {
        return context;
    }

    public @NotNull MentionType getMentionType() {
        return mentionType;
    }

    public @NotNull List<ServerPlayer> getTargets() {
        return targets;
    }

    public @NotNull ServerPlayer getSender() {
        return context.sender();
    }

    public @NotNull Component getOriginalMessage() {
        return context.originalMessage();
    }

    public @NotNull String getRawMessage() {
        return context.rawMessage();
    }

    public @NotNull String getMentionKey() {
        return context.mentionKey();
    }

    public @Nullable String getSingleTargetPlayerName() {
        if (targets.size() != 1) {
            return null;
        }
        return targets.getFirst().getScoreboardName();
    }

    public boolean isSingleTarget() {
        return targets.size() == 1;
    }

    public static class Pre extends MentionEvent implements ICancellableEvent {
        public Pre(@NotNull MentionContext context, @NotNull MentionType mentionType, @NotNull List<ServerPlayer> targets) {
            super(context, mentionType, targets);
        }
    }

    public static class Post extends MentionEvent {
        public Post(@NotNull MentionContext context, @NotNull MentionType mentionType, @NotNull List<ServerPlayer> targets) {
            super(context, mentionType, targets);
        }
    }
}
