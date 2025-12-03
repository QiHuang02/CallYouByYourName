package cn.qihuang02.callyou.core.impl.formatter;

import cn.qihuang02.callyou.api.MentionContext;
import cn.qihuang02.callyou.api.TextFormatter;
import cn.qihuang02.callyou.api.TextFormatterType;
import cn.qihuang02.callyou.registry.BuiltInCallYouRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public record SimpleTextFormatter(String formatter, ChatFormatting color) implements TextFormatter {
    public static final MapCodec<SimpleTextFormatter> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("formatter").forGetter(SimpleTextFormatter::formatter),
            Codec.STRING.optionalFieldOf("color", "white")
                    .xmap(
                            s -> ChatFormatting.valueOf(s.toUpperCase(Locale.ROOT)),
                            ChatFormatting::getName
                    )
                    .forGetter(SimpleTextFormatter::color)
    ).apply(instance, SimpleTextFormatter::new));

    @Contract(pure = true)
    @Override
    public @NotNull TextFormatterType type() {
        return BuiltInCallYouRegistries.SIMPLE_FORMATTER_TYPE.get();
    }

    @Contract(pure = true)
    @Override
    public @NotNull Component format(MentionContext context) {
        return Component.literal(this.formatter).withStyle(this.color);
    }
}
