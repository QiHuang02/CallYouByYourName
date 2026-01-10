package cn.qihuang02.callyou.api.components;

import cn.qihuang02.callyou.api.MentionContext;
import cn.qihuang02.callyou.registry.CallYouRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public interface TextFormatter extends IDispatchedComponent<TextFormatter, TextFormatter.TextFormatterType> {
    Codec<TextFormatter> CODEC = IDispatchedComponent.codec(CallYouRegistries.TEXT_FORMATTER_TYPES);

    @NotNull TextFormatterType type();

    @NotNull Component format(MentionContext context);

    default boolean supportReply() {
        return false;
    }

    default String buildReplySuggestion(@NotNull MentionContext context, @NotNull Component formattedMention) {
        return "";
    }

    record TextFormatterType(
            MapCodec<? extends TextFormatter> mapCodec) implements IDispatchedComponent.Type<TextFormatter> {
    }
}