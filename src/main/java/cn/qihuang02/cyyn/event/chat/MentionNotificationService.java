package cn.qihuang02.cyyn.event.chat;

import cn.qihuang02.cyyn.Config;
import cn.qihuang02.cyyn.network.CYYNMessages;
import cn.qihuang02.cyyn.network.packet.PlayAtSoundPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

public class MentionNotificationService {
    public void notifyPlayer(@NotNull ServerPlayer sender, @NotNull ServerPlayer targetPlayer) {
        Component senderNameComponent = sender.getDisplayName().copy().withStyle(ChatFormatting.YELLOW);
        MutableComponent header = Component.translatable("message.cyyn.notified", senderNameComponent)
                .withStyle(ChatFormatting.GOLD);
        targetPlayer.sendSystemMessage(header, false);

        if (Config.enableMentionSound) {
            CYYNMessages.getChannel().send(
                    PacketDistributor.PLAYER.with(() -> targetPlayer),
                    new PlayAtSoundPacket(Config.MENTION_SOUND_ID)
            );
        }
    }

    public void broadcastCustomChat(@NotNull ServerPlayer sender, @NotNull Component message) {
        MutableComponent chatLine = Component.translatable("chat.type.text", sender.getDisplayName(), message);
        if (sender.getServer() != null) {
            sender.getServer().getPlayerList().broadcastSystemMessage(chatLine, false);
        } else {
            sender.sendSystemMessage(chatLine);
        }
    }

    public @NotNull MutableComponent createReplyReadyMessage(@NotNull Component messageComponent, @NotNull ServerPlayer sender) {
        MutableComponent copy = messageComponent.copy();
        Style replyStyle = createReplyInteractionStyle(sender);
        applyReplyStyle(copy, replyStyle);
        return copy;
    }

    private @NotNull Style createReplyInteractionStyle(@NotNull ServerPlayer sender) {
        return Style.EMPTY
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "@" + sender.getGameProfile().getName() + " "))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("message.cyyn.notified.reply_tooltip")));
    }

    private void applyReplyStyle(@NotNull MutableComponent component, @NotNull Style replyStyle) {
        component.setStyle(replyStyle.applyTo(component.getStyle()));
        for (int i = 0; i < component.getSiblings().size(); i++) {
            Component sibling = component.getSiblings().get(i);
            MutableComponent mutableSibling = sibling.copy();
            applyReplyStyle(mutableSibling, replyStyle);
            component.getSiblings().set(i, mutableSibling);
        }
    }
}
