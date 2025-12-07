package cn.qihuang02.callyou.core.impl.formatter;

import cn.qihuang02.callyou.api.MentionContext;
import cn.qihuang02.callyou.api.TextFormatter;
import cn.qihuang02.callyou.api.TextFormatterType;
import cn.qihuang02.callyou.registry.BuiltInCallYouRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
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

        MutableComponent base = Component.literal("Spot")
                .withStyle(style -> style.withColor(ChatFormatting.GREEN));

        return ComponentUtils.wrapInSquareBrackets(base)
                .withStyle(style -> style
                        .withColor(ChatFormatting.GREEN)
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText)));
    }
}
