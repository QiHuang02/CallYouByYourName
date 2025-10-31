package cn.qihuang02.cyyn.server.chat;

import cn.qihuang02.cyyn.api.mention.MentionFunction;
import cn.qihuang02.cyyn.api.mention.MentionGroup;
import cn.qihuang02.cyyn.api.mention.MentionRegistry;
import cn.qihuang02.cyyn.common.config.Config;
import cn.qihuang02.cyyn.common.mention.MentionParseResult;
import cn.qihuang02.cyyn.common.mention.MentionParser;
import cn.qihuang02.cyyn.server.mention.MentionCooldownTracker;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.ServerChatEvent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

public class MentionChatProcessor {
    private final Supplier<List<MentionFunction>> mentionFunctionsSupplier;
    private final MentionCooldownTracker cooldownTracker;
    private final MentionNotificationService notificationService;
    private final MentionParser mentionParser;

    public MentionChatProcessor(@NotNull Supplier<List<MentionFunction>> mentionFunctionsSupplier,
                                @NotNull MentionCooldownTracker cooldownTracker,
                                @NotNull MentionNotificationService notificationService,
                                @NotNull MentionParser mentionParser) {
        this.mentionFunctionsSupplier = Objects.requireNonNull(mentionFunctionsSupplier, "mentionFunctionsSupplier");
        this.cooldownTracker = Objects.requireNonNull(cooldownTracker, "cooldownTracker");
        this.notificationService = Objects.requireNonNull(notificationService, "notificationService");
        this.mentionParser = Objects.requireNonNull(mentionParser, "mentionParser");
    }

    public static @NotNull MentionChatProcessor createDefault() {
        return new MentionChatProcessor(
                MentionChatProcessor::registeredFunctions,
                MentionCooldownTracker.getInstance(),
                new MentionNotificationService(),
                new MentionParser()
        );
    }

    @Contract(" -> new")
    private static @NotNull List<MentionFunction> registeredFunctions() {
        return MentionRegistry.streamFunctions().toList();
    }

    public boolean process(@NotNull ServerChatEvent event) {
        ServerPlayer sender = event.getPlayer();
        Component originalComponent = event.getMessage();

        Component processedComponent = originalComponent;

        boolean eventCanceled = false;
        boolean broadcastManually = false;
        int maxCooldownTicks = Config.mentionCooldownTicks;
        List<MentionFunction> mentionFunctions = Objects.requireNonNullElseGet(mentionFunctionsSupplier.get(), List::of);
        for (MentionFunction function : mentionFunctions) {
            MentionFunction.Result result = function.format(sender, processedComponent);
            if (result.component() != null) {
                processedComponent = result.component();
                broadcastManually = true;
            }

            if (result.handled()) {
                maxCooldownTicks = Math.max(maxCooldownTicks, function.coolDown());
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
        for (MentionGroup group : mentionParseResult.groups()) {
            maxCooldownTicks = Math.max(maxCooldownTicks, group.coolDown());
        }
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

        long currentTick = sender.serverLevel().getGameTime();
        UUID senderId = sender.getUUID();
        long cooldownTicks = maxCooldownTicks;
        if (cooldownTracker.isOnCooldown(senderId, cooldownTicks, currentTick)) {
            long ticksLeft = cooldownTracker.getRemainingTicks(senderId, cooldownTicks, currentTick);
            long secondsLeft = (ticksLeft + 19L) / 20L;
            sender.sendSystemMessage(
                    Component.translatable("message.cyyn.cooldown", secondsLeft)
                            .withStyle(ChatFormatting.RED)
            );
            event.setCanceled(true);
            return true;
        }

        for (ServerPlayer targetPlayer : mentionedPlayers) {
            notificationService.notifyPlayer(sender, targetPlayer);
        }

        cooldownTracker.updateCooldown(senderId, currentTick);
        return eventCanceled;
    }
}
