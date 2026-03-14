package cn.qihuang02.callyou.core.mention.components.formatter;

import cn.qihuang02.callyou.api.MentionCandidate;
import cn.qihuang02.callyou.api.MentionContext;
import cn.qihuang02.callyou.api.components.InteractionDecorator;
import cn.qihuang02.callyou.api.components.TextFormatter;
import cn.qihuang02.callyou.core.mention.components.decorator.CompositeInteractionDecorator;
import cn.qihuang02.callyou.core.mention.components.decorator.ReplyDecorator;
import cn.qihuang02.callyou.core.mention.components.decorator.TextColorDecorator;
import cn.qihuang02.callyou.registry.BuiltInCallYouRegistries;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public record PlayerNameTextFormatter(InteractionDecorator decorator) implements TextFormatter {
    public static final MapCodec<PlayerNameTextFormatter> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            InteractionDecorator.CODEC.optionalFieldOf("decorator")
                    .forGetter(formatter -> Optional.ofNullable(formatter.decorator))
    ).apply(instance, decoratorOpt -> new PlayerNameTextFormatter(decoratorOpt.orElse(null))));
    private static final InteractionDecorator DEFAULT_DECORATOR = new CompositeInteractionDecorator(List.of(
            new TextColorDecorator(ChatFormatting.YELLOW),
            new ReplyDecorator(ReplyDecorator.ReplyScope.TARGET)
    ));

    public PlayerNameTextFormatter(InteractionDecorator decorator) {
        this.decorator = decorator != null ? decorator : DEFAULT_DECORATOR;
    }

    @Override
    public @NotNull TextFormatterType type() {
        return BuiltInCallYouRegistries.PLAYER_NAME_FORMATTER_TYPE;
    }

    @Override
    public @NotNull Component format(@NotNull MentionContext context, @NotNull MentionCandidate candidate) {
        String token = candidate.mentionToken();
        if ("@".equals(token)) {
            token = "@player";
        }
        return Component.literal(token);
    }

    @Override
    public @NotNull InteractionDecorator decorator() {
        return this.decorator;
    }
}
