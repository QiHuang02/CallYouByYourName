package cn.qihuang02.callyou.api;

import cn.qihuang02.callyou.registry.CallYouRegistries;
import com.mojang.serialization.Codec;
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
}