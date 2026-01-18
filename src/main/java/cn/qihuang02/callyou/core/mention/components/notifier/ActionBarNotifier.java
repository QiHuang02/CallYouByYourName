package cn.qihuang02.callyou.core.mention.components.notifier;

import cn.qihuang02.callyou.api.MentionCandidate;
import cn.qihuang02.callyou.api.MentionContext;
import cn.qihuang02.callyou.api.components.Notifier;
import cn.qihuang02.callyou.registry.BuiltInCallYouRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record ActionBarNotifier(String messageKey, boolean useSenderName) implements Notifier {
    public static final MapCodec<ActionBarNotifier> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.optionalFieldOf("message_key", "message.callyou.notify.default").forGetter(ActionBarNotifier::messageKey),
            Codec.BOOL.optionalFieldOf("use_sender_name", true).forGetter(ActionBarNotifier::useSenderName)
    ).apply(instance, ActionBarNotifier::new));

    @Override
    public @NotNull NotifierType type() {
        return BuiltInCallYouRegistries.ACTION_BAR_TYPE.get();
    }

    @Override
    public void apply(
            @NotNull MentionContext context,
            @NotNull MentionCandidate candidate,
            @NotNull List<ServerPlayer> targets
    ) {
        Component text;
        if (useSenderName) {
            text = Component.translatable(messageKey, context.senderDisplayName()).withStyle(ChatFormatting.GOLD);
        } else {
            text = Component.translatable(messageKey).withStyle(ChatFormatting.GOLD);
        }

        for (ServerPlayer target : targets) {
            target.displayClientMessage(text, true);
        }
    }
}
