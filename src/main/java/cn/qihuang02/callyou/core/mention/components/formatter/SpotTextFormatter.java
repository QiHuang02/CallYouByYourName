package cn.qihuang02.callyou.core.mention.components.formatter;

import cn.qihuang02.callyou.api.MentionCandidate;
import cn.qihuang02.callyou.api.MentionContext;
import cn.qihuang02.callyou.api.components.InteractionDecorator;
import cn.qihuang02.callyou.api.components.TextFormatter;
import cn.qihuang02.callyou.compat.ftb.FTBChunksAPIWrapper;
import cn.qihuang02.callyou.core.mention.components.decorator.CompositeInteractionDecorator;
import cn.qihuang02.callyou.core.mention.components.decorator.SquareBracketsDecorator;
import cn.qihuang02.callyou.core.mention.components.decorator.TextColorDecorator;
import cn.qihuang02.callyou.registry.BuiltInCallYouRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public record SpotTextFormatter(InteractionDecorator decorator) implements TextFormatter {
    public static final InteractionDecorator DEFAULT_DECORATOR = new CompositeInteractionDecorator(List.of(
            new TextColorDecorator(ChatFormatting.GREEN),
            SquareBracketsDecorator.INSTANCE
    ));

    public static final MapCodec<SpotTextFormatter> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            InteractionDecorator.CODEC.optionalFieldOf("decorator")
                    .forGetter(formatter -> Optional.ofNullable(formatter.decorator))
    ).apply(instance, decoratorOpt -> new SpotTextFormatter(decoratorOpt.orElse(null))));

    public static final Codec<SpotTextFormatter> CODEC = MAP_CODEC.codec();

    public SpotTextFormatter(InteractionDecorator decorator) {
        this.decorator = decorator != null ? decorator : DEFAULT_DECORATOR;
    }

    @Override
    public @NotNull TextFormatterType type() {
        return BuiltInCallYouRegistries.SPOT_FORMATTER_TYPE;
    }

    @Override
    public @NotNull Component format(@NotNull MentionContext context, @NotNull MentionCandidate candidate) {
        BlockPos pos = context.sender().blockPosition();

        MutableComponent location = Component.translatable("message.callyou.spot", pos.getX(), pos.getY(), pos.getZ());
        MutableComponent hoverText = Component.empty().append(location);

        boolean ftbLoaded = FTBChunksAPIWrapper.isLoaded();
        final String command;
        if (ftbLoaded) {
            hoverText.append(Component.literal("\n"))
                    .append(Component.translatable("message.callyou.spot.ftb.add"));
            String sharedName = Component.translatable("message.callyou.spot.ftb.shared_by", context.senderName()).getString();
            command = FTBChunksAPIWrapper.buildTransientWaypointCommand(
                    context.sender().level().dimension(),
                    pos,
                    sharedName
            );
        } else {
            command = null;
        }

        MutableComponent base = Component.translatable("message.callyou.spot.label")
                .withStyle(style -> {
                    Style updated = style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));
                    if (ftbLoaded && command != null) {
                        updated = updated.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
                    }
                    return updated;
                });

        return base;
    }

    @Override
    public @NotNull InteractionDecorator decorator() {
        return this.decorator;
    }
}
