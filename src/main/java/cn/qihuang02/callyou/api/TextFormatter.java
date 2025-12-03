package cn.qihuang02.callyou.api;

import cn.qihuang02.callyou.registry.CallYouRegistries;
import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;

public interface TextFormatter {
    Codec<TextFormatter> CODEC =
            CallYouRegistries.TEXT_FORMATTER_TYPES
                    .byNameCodec()
                    .dispatch(
                            "type",
                            TextFormatter::type,
                            TextFormatterType::mapCodec
                    );

    TextFormatterType type();

    Component format(MentionContext context);
}