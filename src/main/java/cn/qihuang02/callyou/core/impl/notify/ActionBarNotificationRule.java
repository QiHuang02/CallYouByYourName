package cn.qihuang02.callyou.core.impl.notify;

import cn.qihuang02.callyou.api.MentionContext;
import cn.qihuang02.callyou.api.NotificationRule;
import cn.qihuang02.callyou.api.NotificationRuleType;
import cn.qihuang02.callyou.registry.BuiltInCallYouRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;

public record ActionBarNotificationRule(String messageKey, boolean useSenderName) implements NotificationRule {
    public static final MapCodec<ActionBarNotificationRule> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.optionalFieldOf("message_key", "message.callyou.notify.default").forGetter(ActionBarNotificationRule::messageKey),
            Codec.BOOL.optionalFieldOf("use_sender_name", true).forGetter(ActionBarNotificationRule::useSenderName)
    ).apply(instance, ActionBarNotificationRule::new));

    @Override
    public @NotNull NotificationRuleType type() {
        return BuiltInCallYouRegistries.ACTION_BAR_TYPE.get();
    }

    @Override
    public void apply(MentionContext context, List<ServerPlayer> targets) {
        Component text;
        if (useSenderName) {
            text = Component.translatable(messageKey, context.sender().getDisplayName()).withStyle(ChatFormatting.GOLD);
        } else {
            text = Component.translatable(messageKey).withStyle(ChatFormatting.GOLD);
        }

        for (ServerPlayer target : targets) {
            target.displayClientMessage(text, true);
        }
    }
}
