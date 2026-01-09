package cn.qihuang02.callyou.api.components;

import cn.qihuang02.callyou.api.MentionContext;
import cn.qihuang02.callyou.registry.CallYouRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public interface TextFormatter {
    Codec<TextFormatter> CODEC =
            CallYouRegistries.TEXT_FORMATTER_TYPES
                    .byNameCodec()
                    .dispatch(
                            "type",
                            TextFormatter::type,
                            TextFormatterType::mapCodec
                    );

    @NotNull TextFormatterType type();

    @NotNull Component format(MentionContext context);

    default boolean supportReply() {
        return false;
    }

    default String buildReplySuggestion(@NotNull MentionContext context, @NotNull Component formattedMention) {
        return "";
    }

    record TextFormatterType(MapCodec<? extends TextFormatter> mapCodec) {
        @SuppressWarnings("unchecked")
        public Codec<TextFormatter> codec() {
            return (Codec<TextFormatter>) mapCodec.codec();
        }
    }
}