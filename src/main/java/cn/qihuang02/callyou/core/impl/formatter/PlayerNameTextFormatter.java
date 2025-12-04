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

import java.util.Locale;

public record PlayerNameTextFormatter(ChatFormatting color) implements TextFormatter {
    public static final MapCodec<PlayerNameTextFormatter> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.optionalFieldOf("color", "yellow")
                    .xmap(
                            s -> ChatFormatting.valueOf(s.toUpperCase(Locale.ROOT)),
                            ChatFormatting::getName
                    )
                    .forGetter(PlayerNameTextFormatter::color)
    ).apply(instance, PlayerNameTextFormatter::new));

    @Override
    public TextFormatterType type() {
        return BuiltInCallYouRegistries.PLAYER_NAME_FORMATTER_TYPE.get();
    }

    @Override
    public Component format(MentionContext context) {
        String key = context.mentionKey();
        if (key == null || key.isEmpty()) {
            key = "player";
        }
        return Component.literal("@" + key).withStyle(this.color);
    }
}
