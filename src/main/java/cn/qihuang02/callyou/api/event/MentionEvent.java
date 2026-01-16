package cn.qihuang02.callyou.api.event;

import cn.qihuang02.callyou.api.MentionContext;
import cn.qihuang02.callyou.api.MentionType;
import cn.qihuang02.callyou.api.TargetCollection;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.function.Predicate;

public abstract class MentionEvent extends Event {
    private final MentionContext context;
    private final MentionType mentionType;
    private TargetCollection targets;

    protected MentionEvent(@NotNull MentionContext context, @NotNull MentionType mentionType, @NotNull TargetCollection targets) {
        this.context = context;
        this.mentionType = mentionType;
        this.targets = targets;
    }

    public @NotNull MentionContext getContext() {
        return context;
    }

    public @NotNull MentionType getMentionType() {
        return mentionType;
    }

    public @NotNull TargetCollection getTargets() {
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

    protected void setTargetsInternal(@NotNull TargetCollection targets) {
        this.targets = targets;
    }

    public static class Post extends MentionEvent {
        public Post(@NotNull MentionContext context, @NotNull MentionType mentionType, @NotNull TargetCollection targets) {
            super(context, mentionType, targets);
        }
    }

    public static final class Pre extends MentionEvent implements ICancellableEvent {
        public Pre(
                @NotNull MentionContext context,
                @NotNull MentionType mentionType,
                @NotNull TargetCollection targets
        ) {
            super(context, mentionType, targets);
        }

        public void setTargets(@NotNull TargetCollection newTargets) {
            setTargetsInternal(newTargets);
        }

        public void removeTargets(@NotNull Predicate<UUID> predicate) {
            setTargetsInternal(getTargets().removeIf(predicate));
        }
    }
}
