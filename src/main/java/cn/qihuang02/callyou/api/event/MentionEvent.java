package cn.qihuang02.callyou.api.event;

import cn.qihuang02.callyou.api.MentionCandidate;
import cn.qihuang02.callyou.api.MentionContext;
import cn.qihuang02.callyou.api.MentionType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.Event.HasResult;
import org.jetbrains.annotations.NotNull;


public abstract class MentionEvent extends Event {
    private final MentionContext context;
    private final MentionCandidate candidate;

    protected MentionEvent(@NotNull MentionContext context, @NotNull MentionCandidate candidate) {
        this.context = context;
        this.candidate = candidate;
    }

    public @NotNull MentionContext getContext() {
        return context;
    }

    public @NotNull MentionCandidate getCandidate() {
        return candidate;
    }

    public @NotNull MentionType getMentionType() {
        return candidate.type();
    }

    public @NotNull ServerPlayer getSender() {
        return context.sender();
    }

    public @NotNull Component getOriginalMessage() {
        return context.originalMessage();
    }

    public @NotNull String getRawMessage() {
        return context.rawText();
    }

    public @NotNull String getMentionKey() {
        return candidate.key();
    }

    public static class Post extends MentionEvent {
        public Post(@NotNull MentionContext context, @NotNull MentionCandidate candidate) {
            super(context, candidate);
        }
    }

    @net.minecraftforge.eventbus.api.Cancelable
    public static final class Pre extends MentionEvent {
        public Pre(
                @NotNull MentionContext context,
                @NotNull MentionCandidate candidate
        ) {
            super(context, candidate);
        }
    }
}
