package cn.qihuang02.cyyn.event.chat;

import cn.qihuang02.cyyn.Config;
import cn.qihuang02.cyyn.network.CYYNMessages;
import cn.qihuang02.cyyn.network.packet.PlayAtSoundPacket;
import cn.qihuang02.cyyn.util.MentionParseResult;
import cn.qihuang02.cyyn.util.MentionParser;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public record MentionChatProcessor(@NotNull ItemMentionFormatter itemMentionFormatter) {
    private static final MentionChatProcessor INSTANCE = new MentionChatProcessor(new ItemMentionFormatter());
    private static final Map<UUID, Long> PLAYER_COOLDOWN_MAP = new ConcurrentHashMap<>();

    public MentionChatProcessor {
        Objects.requireNonNull(itemMentionFormatter, "itemMentionFormatter");
    }

    @NotNull
    public static MentionChatProcessor getInstance() {
        return INSTANCE;
    }

    public boolean process(@NotNull ServerChatEvent event) {
        ServerPlayer sender = event.getPlayer();
        Component originalComponent = event.getMessage();

        ItemMentionFormatter.FormatResult formatResult = itemMentionFormatter().format(sender, originalComponent);
        Component processedComponent = formatResult.component() != null ? formatResult.component() : originalComponent;

        boolean eventCanceled = false;
        boolean broadcastManually = false;
        if (formatResult.canceled()) {
            event.setCanceled(true);
            eventCanceled = true;
            if (formatResult.component() == null) {
                return true;
            }
            broadcastManually = true;
        }

        String message = processedComponent.getString();
        if (!message.contains("@")) {
            if (broadcastManually) {
                broadcastCustomChat(sender, processedComponent);
            }
            return eventCanceled;
        }

        MentionParseResult mentionParseResult = MentionParser.parse(message, sender);
        if (mentionParseResult.deniedGroupMention()) {
            sender.sendSystemMessage(
                    Component.translatable("message.cyyn.group.denied")
                            .withStyle(ChatFormatting.RED)
            );
        }

        List<ServerPlayer> mentionedPlayers = mentionParseResult.players();
        if (mentionedPlayers.isEmpty()) {
            if (broadcastManually) {
                broadcastCustomChat(sender, processedComponent);
            }
            return eventCanceled;
        }

        MutableComponent replyReadyMessage = createReplyReadyMessage(processedComponent, sender);
        processedComponent = replyReadyMessage;

        if (broadcastManually) {
            broadcastCustomChat(sender, processedComponent);
        } else {
            event.setMessage(processedComponent);
        }

        long currentTime = System.currentTimeMillis();
        UUID senderId = sender.getUUID();
        long lastAtTime = PLAYER_COOLDOWN_MAP.getOrDefault(senderId, 0L);
        long cooldownMs = Config.mentionCooldownMs;
        if (currentTime - lastAtTime < cooldownMs) {
            long timeLeft = (cooldownMs - (currentTime - lastAtTime)) / 1000L;
            sender.sendSystemMessage(
                    Component.translatable("message.cyyn.cooldown", (timeLeft + 1))
                            .withStyle(ChatFormatting.RED)
            );
            event.setCanceled(true);
            return true;
        }

        for (ServerPlayer targetPlayer : mentionedPlayers) {
            notifyPlayer(sender, targetPlayer);
        }

        PLAYER_COOLDOWN_MAP.put(senderId, currentTime);
        return eventCanceled;
    }

    private void notifyPlayer(@NotNull ServerPlayer sender, @NotNull ServerPlayer targetPlayer) {
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

    private @NotNull MutableComponent createReplyReadyMessage(@NotNull Component messageComponent, @NotNull ServerPlayer sender) {
        MutableComponent copy = messageComponent.copy();
        Style replyStyle = createReplyInteractionStyle(sender);
        applyReplyStyle(copy, replyStyle);
        return copy;
    }

    private void broadcastCustomChat(@NotNull ServerPlayer sender, @NotNull Component message) {
        MutableComponent chatLine = Component.translatable("chat.type.text", sender.getDisplayName(), message);
        if (sender.getServer() != null) {
            sender.getServer().getPlayerList().broadcastSystemMessage(chatLine, false);
        } else {
            sender.sendSystemMessage(chatLine);
        }
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
