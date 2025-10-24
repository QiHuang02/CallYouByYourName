package cn.qihuang02.cyyn.event;

import cn.qihuang02.cyyn.CallYouByYourName;
import cn.qihuang02.cyyn.Config;
import cn.qihuang02.cyyn.network.CYYNMessages;
import cn.qihuang02.cyyn.network.PlayAtSoundPacket;
import cn.qihuang02.cyyn.util.MentionParseResult;
import cn.qihuang02.cyyn.util.MentionParser;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = CallYouByYourName.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CYYNServerChatEvent {
    private static final Map<UUID, Long> PLAYER_COOLDOWN_MAP = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onServerChat(@NotNull ServerChatEvent event) {
        ServerPlayer sender = event.getPlayer();
        String message = event.getMessage().getString();
        UUID senderId = sender.getUUID();

        if (!message.contains("@")) {
            return;
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
            return;
        }

        long currentTime = System.currentTimeMillis();

        long lastAtTime = PLAYER_COOLDOWN_MAP.getOrDefault(senderId, 0L);
        long cooldownMs = Config.mentionCooldownMs;
        if (currentTime - lastAtTime < cooldownMs) {
            long timeLeft = (cooldownMs - (currentTime - lastAtTime)) / 1000L;
            sender.sendSystemMessage(
                    Component.translatable("message.cyyn.cooldown", (timeLeft + 1))
                            .withStyle(ChatFormatting.RED)
            );

            event.setCanceled(true);
            return;
        }

        for (ServerPlayer targetPlayer : mentionedPlayers) {
            Component senderNameComponent = sender.getDisplayName().copy().withStyle(ChatFormatting.YELLOW);
            Component replyComponent = event.getMessage().copy()
                    .withStyle(style -> style
                            .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "@" + sender.getGameProfile().getName() + " "))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("message.cyyn.notified.reply_tooltip")))
                            .withColor(ChatFormatting.YELLOW));
            Component atMessage = Component.translatable("message.cyyn.notified", senderNameComponent, replyComponent)
                    .withStyle(ChatFormatting.GOLD);
            targetPlayer.sendSystemMessage(atMessage, false);

            if (Config.enableMentionSound) {
                CYYNMessages.getChannel().send(
                        PacketDistributor.PLAYER.with(() -> targetPlayer),
                        new PlayAtSoundPacket(Config.MENTION_SOUND_ID)
                );
            }
        }

        PLAYER_COOLDOWN_MAP.put(senderId, currentTime);
    }
}
