package cn.qihuang02.callyou.core.mention.components.decorator;

import cn.qihuang02.callyou.api.MentionCandidate;
import cn.qihuang02.callyou.api.MentionContext;
import cn.qihuang02.callyou.api.MentionView;
import cn.qihuang02.callyou.api.components.InteractionDecorator;
import cn.qihuang02.callyou.registry.BuiltInCallYouRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public record ReplyDecorator(ReplyScope scope) implements InteractionDecorator {
    public static final MapCodec<ReplyDecorator> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.optionalFieldOf("scope", "target")
                    .xmap(
                            value -> ReplyScope.valueOf(value.toUpperCase(Locale.ROOT)),
                            scope -> scope.name().toLowerCase(Locale.ROOT)
                    )
                    .forGetter(ReplyDecorator::scope)
    ).apply(instance, ReplyDecorator::new));

    private static Style applyReplyStyle(@NotNull Style style, @NotNull HoverEvent hoverEvent, @NotNull String suggestion) {
        Style updated = style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, suggestion));
        if (style.getHoverEvent() == null) {
            updated = updated.withHoverEvent(hoverEvent);
        }
        return updated;
    }

    @Override
    public @NotNull InteractionDecoratorType type() {
        return BuiltInCallYouRegistries.REPLY_DECORATOR_TYPE;
    }

    @Override
    public @NotNull Component decorate(
            @NotNull Component original,
            @NotNull MentionContext context,
            @NotNull MentionCandidate candidate,
            @NotNull MentionView view
    ) {
        if (!this.scope.appliesTo(view)) {
            return original;
        }
        String senderName = context.senderName();
        if (senderName == null || senderName.isBlank()) {
            return original;
        }
        String suggestion = "@" + senderName + " ";
        HoverEvent hoverEvent = new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                Component.literal("Reply " + senderName)
        );
        return original.copy().withStyle(style -> applyReplyStyle(style, hoverEvent, suggestion));
    }

    public enum ReplyScope {
        TARGET,
        SENDER,
        BOTH;

        public boolean appliesTo(@NotNull MentionView view) {
            if (this == BOTH) return true;
            if (this == TARGET && view == MentionView.TARGET) return true;
            if (this == SENDER && view == MentionView.SENDER) return true;
            return false;
        }
    }
}
