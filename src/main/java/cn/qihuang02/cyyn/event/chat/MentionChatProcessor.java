package cn.qihuang02.cyyn.event.chat;

import cn.qihuang02.cyyn.Config;
import cn.qihuang02.cyyn.mention.formatter.ItemMentionFormatter;
import cn.qihuang02.cyyn.mention.formatter.MentionFormatter;
import cn.qihuang02.cyyn.mention.formatter.SpotMentionFormatter;
import cn.qihuang02.cyyn.util.MentionParseResult;
import cn.qihuang02.cyyn.util.MentionParser;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.ServerChatEvent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class MentionChatProcessor {
    private final List<MentionFormatter> mentionFormatters;
    private final MentionCooldownTracker cooldownTracker;
    private final MentionNotificationService notificationService;
    private final MentionParser mentionParser;

    public MentionChatProcessor(@NotNull List<MentionFormatter> mentionFormatters,
                                @NotNull MentionCooldownTracker cooldownTracker,
                                @NotNull MentionNotificationService notificationService,
                                @NotNull MentionParser mentionParser) {
        this.mentionFormatters = List.copyOf(Objects.requireNonNull(mentionFormatters, "mentionFormatters"));
        this.cooldownTracker = Objects.requireNonNull(cooldownTracker, "cooldownTracker");
        this.notificationService = Objects.requireNonNull(notificationService, "notificationService");
        this.mentionParser = Objects.requireNonNull(mentionParser, "mentionParser");
    }

    public static @NotNull MentionChatProcessor createDefault() {
        return new MentionChatProcessor(
                defaultFormatters(),
                MentionCooldownTracker.getInstance(),
                new MentionNotificationService(),
                new MentionParser()
        );
    }

    @Contract(" -> new")
    private static @NotNull @Unmodifiable List<MentionFormatter> defaultFormatters() {
        return List.of(
                new ItemMentionFormatter(),
                new SpotMentionFormatter()
        );
    }

    public boolean process(@NotNull ServerChatEvent event) {
        ServerPlayer sender = event.getPlayer();
        Component originalComponent = event.getMessage();

        Component processedComponent = originalComponent;

        boolean eventCanceled = false;
        boolean broadcastManually = false;
        for (MentionFormatter formatter : mentionFormatters) {
            MentionFormatter.Result result = formatter.format(sender, processedComponent);
            if (result.component() != null) {
                processedComponent = result.component();
                broadcastManually = true;
            }

            if (result.cancelEvent()) {
                event.setCanceled(true);
                eventCanceled = true;
                if (result.component() == null) {
                    return true;
                }
            }
        }

        if (broadcastManually) {
            event.setCanceled(true);
            eventCanceled = true;
        }

        String message = processedComponent.getString();
        if (!message.contains("@")) {
            if (broadcastManually) {
                notificationService.broadcastCustomChat(sender, processedComponent);
            }
            return eventCanceled;
        }

        MentionParseResult mentionParseResult = mentionParser.parse(message, sender);
        if (mentionParseResult.deniedGroupMention()) {
            sender.sendSystemMessage(
                    Component.translatable("message.cyyn.group.denied")
                            .withStyle(ChatFormatting.RED)
            );
        }

        List<ServerPlayer> mentionedPlayers = mentionParseResult.players();
        if (mentionedPlayers.isEmpty()) {
            if (broadcastManually) {
                notificationService.broadcastCustomChat(sender, processedComponent);
            }
            return eventCanceled;
        }

        MutableComponent replyReadyMessage = notificationService.createReplyReadyMessage(processedComponent, sender);
        processedComponent = replyReadyMessage;

        if (broadcastManually) {
            notificationService.broadcastCustomChat(sender, processedComponent);
        } else {
            event.setMessage(processedComponent);
        }

        long currentTime = System.currentTimeMillis();
        UUID senderId = sender.getUUID();
        long cooldownMs = Config.mentionCooldownMs;
        if (cooldownTracker.isOnCooldown(senderId, cooldownMs, currentTime)) {
            long timeLeft = cooldownTracker.getRemainingSeconds(senderId, cooldownMs, currentTime);
            sender.sendSystemMessage(
                    Component.translatable("message.cyyn.cooldown", (timeLeft + 1))
                            .withStyle(ChatFormatting.RED)
            );
            event.setCanceled(true);
            return true;
        }

        for (ServerPlayer targetPlayer : mentionedPlayers) {
            notificationService.notifyPlayer(sender, targetPlayer);
        }

        cooldownTracker.updateCooldown(senderId, currentTime);
        return eventCanceled;
    }
}
