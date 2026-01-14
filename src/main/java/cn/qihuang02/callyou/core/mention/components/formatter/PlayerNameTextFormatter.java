package cn.qihuang02.callyou.core.mention.components.formatter;

import cn.qihuang02.callyou.api.MentionContext;
import cn.qihuang02.callyou.api.components.TextFormatter;
import cn.qihuang02.callyou.registry.BuiltInCallYouRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

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
    public @NotNull TextFormatterType type() {
        return BuiltInCallYouRegistries.PLAYER_NAME_FORMATTER_TYPE.get();
    }

    @Override
    public @NotNull Component format(@NotNull MentionContext context) {
        String key = context.mentionKey();
        String token = key == null || key.isEmpty() ? "@player" : context.mentionToken();
        return Component.literal(token).withStyle(this.color);
    }

    @Override
    public boolean supportReply() {
        return true;
    }

    @Override
    public @NotNull String buildReplySuggestion(@NotNull MentionContext context, @NotNull Component formattedMention) {
        String senderName = context.senderName();
        if (senderName == null || senderName.isBlank()) {
            return "";
        }
        return "@" + senderName + " ";
    }
}
