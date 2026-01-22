package cn.qihuang02.callyou.core.mention.components.formatter;

import cn.qihuang02.callyou.api.MentionCandidate;
import cn.qihuang02.callyou.api.MentionContext;
import cn.qihuang02.callyou.api.components.InteractionDecorator;
import cn.qihuang02.callyou.api.components.TextFormatter;
import cn.qihuang02.callyou.core.mention.components.decorator.CompositeInteractionDecorator;
import cn.qihuang02.callyou.core.mention.components.decorator.ItemRenderDecorator;
import cn.qihuang02.callyou.core.mention.components.decorator.RarityStyleDecorator;
import cn.qihuang02.callyou.core.mention.components.decorator.SquareBracketsDecorator;
import cn.qihuang02.callyou.registry.BuiltInCallYouRegistries;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public record ItemTextFormatter(InteractionDecorator decorator) implements TextFormatter {
    public static final InteractionDecorator DEFAULT_DECORATOR = new CompositeInteractionDecorator(List.of(
            ItemRenderDecorator.INSTANCE,
            RarityStyleDecorator.INSTANCE,
            SquareBracketsDecorator.INSTANCE
    ));

    public static final MapCodec<ItemTextFormatter> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            InteractionDecorator.CODEC.optionalFieldOf("decorator")
                    .forGetter(formatter -> Optional.ofNullable(formatter.decorator))
    ).apply(instance, decoratorOpt -> new ItemTextFormatter(decoratorOpt.orElse(null))));

    public ItemTextFormatter(InteractionDecorator decorator) {
        this.decorator = decorator != null ? decorator : DEFAULT_DECORATOR;
    }

    @Override
    public @NotNull TextFormatterType type() {
        return BuiltInCallYouRegistries.ITEM_FORMATTER_TYPE.get();
    }

    @Override
    public @NotNull Component format(@NotNull MentionContext context, @NotNull MentionCandidate candidate) {
        ServerPlayer sender = context.sender();
        if (sender == null) {
            return Component.literal("@item");
        }

        ItemStack stack = sender.getMainHandItem();
        if (stack.isEmpty()) {
            return Component.literal(candidate.mentionToken());
        }

        return stack.getHoverName().copy();
    }

    @Override
    public @NotNull InteractionDecorator decorator() {
        return this.decorator;
    }
}
