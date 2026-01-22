package cn.qihuang02.callyou.core.mention.components.formatter;

import cn.qihuang02.callyou.api.MentionCandidate;
import cn.qihuang02.callyou.api.MentionContext;
import cn.qihuang02.callyou.api.components.InteractionDecorator;
import cn.qihuang02.callyou.api.components.NoopInteractionDecorator;
import cn.qihuang02.callyou.api.components.TextFormatter;
import cn.qihuang02.callyou.registry.BuiltInCallYouRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record ModularTextFormatter(String template, InteractionDecorator decorator) implements TextFormatter {
    public static final MapCodec<ModularTextFormatter> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("template").forGetter(ModularTextFormatter::template),
            InteractionDecorator.CODEC.optionalFieldOf("decorator")
                    .forGetter(formatter -> Optional.ofNullable(formatter.decorator))
    ).apply(instance, (template, decoratorOpt) -> new ModularTextFormatter(template, decoratorOpt.orElse(null))));

    public ModularTextFormatter(String template, InteractionDecorator decorator) {
        this.template = template;
        this.decorator = decorator != null ? decorator : NoopInteractionDecorator.INSTANCE;
    }

    @Override
    public @NotNull TextFormatterType type() {
        return BuiltInCallYouRegistries.MODULAR_FORMATTER_TYPE.get();
    }

    @Override
    public @NotNull Component format(@NotNull MentionContext context, @NotNull MentionCandidate candidate) {
        return Component.literal(resolveTemplate(context, candidate));
    }

    @Override
    public @NotNull InteractionDecorator decorator() {
        return this.decorator;
    }

    private @NotNull String resolveTemplate(@NotNull MentionContext context, @NotNull MentionCandidate candidate) {
        String senderName = context.senderName();
        if (senderName == null) {
            senderName = "";
        }
        String key = candidate.key();
        if (key == null) {
            key = "";
        }
        return this.template
                .replace("{token}", candidate.mentionToken())
                .replace("{key}", key)
                .replace("{sender}", senderName);
    }
}
