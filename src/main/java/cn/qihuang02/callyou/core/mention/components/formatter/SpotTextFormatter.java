package cn.qihuang02.callyou.core.mention.components.formatter;

import cn.qihuang02.callyou.api.MentionContext;
import cn.qihuang02.callyou.api.components.TextFormatter;
import cn.qihuang02.callyou.compat.ftb.FTBChunksAPIWrapper;
import cn.qihuang02.callyou.registry.BuiltInCallYouRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;

public enum SpotTextFormatter implements TextFormatter {
    INSTANCE;

    public static final MapCodec<SpotTextFormatter> MAP_CODEC = MapCodec.unit(INSTANCE);
    public static final Codec<SpotTextFormatter> CODEC = MAP_CODEC.codec();

    @Override
    public @NotNull TextFormatterType type() {
        return BuiltInCallYouRegistries.SPOT_FORMATTER_TYPE.get();
    }

    @Override
    public @NotNull Component format(@NotNull MentionContext context) {
        BlockPos pos = context.sender().blockPosition();

        MutableComponent location = Component.translatable("message.callyou.spot", pos.getX(), pos.getY(), pos.getZ());
        MutableComponent hoverText = Component.empty().append(location);

        if (FTBChunksAPIWrapper.isLoaded()) {
            hoverText.append(Component.literal("\n"))
                    .append(Component.translatable("message.callyou.spot.ftb.add"));
        }

        MutableComponent base = Component.translatable("message.callyou.spot.label")
                .withStyle(style -> style.withColor(ChatFormatting.GREEN));

        if (FTBChunksAPIWrapper.isLoaded()) {
            String sharedName = Component.translatable("message.callyou.spot.ftb.shared_by", context.senderName()).getString();
            String command = FTBChunksAPIWrapper.buildTransientWaypointCommand(
                    context.sender().level().dimension(),
                    pos,
                    sharedName
            );

            base.withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command)));
        }

        return ComponentUtils.wrapInSquareBrackets(base)
                .withStyle(style -> style
                        .withColor(ChatFormatting.GREEN)
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText)));
    }
}
