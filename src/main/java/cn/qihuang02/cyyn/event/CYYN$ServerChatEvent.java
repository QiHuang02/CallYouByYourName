package cn.qihuang02.cyyn.event;

import cn.qihuang02.cyyn.CallYouByYourName;
import cn.qihuang02.cyyn.network.CYYN$Messages;
import cn.qihuang02.cyyn.network.ClientboundPlayAtSoundPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = CallYouByYourName.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CYYN$ServerChatEvent {
    private static final long AT_COOLDOWN_MS = 5000;
    private static final Map<UUID, Long> PLAYER_COOLDOWN_MAP = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onServerChat(@NotNull ServerChatEvent event) {
        ServerPlayer sender = event.getPlayer();
        String message = event.getMessage().getString();
        UUID senderId = sender.getUUID();

        if (!message.contains("@")) {
            return;
        }

        List<ServerPlayer> mentionedPlayers = getValidMentionedPlayers(message, sender);

        if (mentionedPlayers.isEmpty()) {
            return;
        }

        long currentTime = System.currentTimeMillis();

        long lastAtTime = PLAYER_COOLDOWN_MAP.getOrDefault(senderId, 0L);
        if (currentTime - lastAtTime < AT_COOLDOWN_MS) {
            long timeLeft = (AT_COOLDOWN_MS - (currentTime - lastAtTime)) / 1000L;
            sender.sendSystemMessage(
                    Component.translatable("message.atatyou.cooldown", (timeLeft + 1))
                            .withStyle(ChatFormatting.RED)
            );

            event.setCanceled(true);
            return;
        }

        for (ServerPlayer targetPlayer : mentionedPlayers) {
            Component senderNameComponent = sender.getDisplayName().copy().withStyle(ChatFormatting.YELLOW);
            Component atMessage = Component.translatable("message.atatyou.notified", senderNameComponent)
                    .withStyle(ChatFormatting.GOLD);
            targetPlayer.sendSystemMessage(atMessage, false);

            CYYN$Messages.getChannel().send(
                    PacketDistributor.PLAYER.with(() -> targetPlayer),
                    new ClientboundPlayAtSoundPacket()
            );
        }

        PLAYER_COOLDOWN_MAP.put(senderId, currentTime);
    }

    private static @NotNull List<ServerPlayer> getValidMentionedPlayers(@NotNull String message, @NotNull ServerPlayer sender) {
        List<ServerPlayer> mentionedPlayers = new ArrayList<>();
        Set<UUID> alreadyMentioned = new HashSet<>();
        PlayerList playerList = Objects.requireNonNull(sender.getServer()).getPlayerList();

        for (String word : message.split(" ")) {
            if (word.startsWith("@") && word.length() > 1) {
                String playerName = word.substring(1);
                ServerPlayer targetPlayer = playerList.getPlayerByName(playerName);

                if (targetPlayer != null && !targetPlayer.equals(sender) && alreadyMentioned.add(targetPlayer.getUUID())) {
                    mentionedPlayers.add(targetPlayer);
                }
            }
        }
        return mentionedPlayers;
    }
}
