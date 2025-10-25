package cn.qihuang02.cyyn.event;

import cn.qihuang02.cyyn.CallYouByYourName;
import cn.qihuang02.cyyn.Config;
import cn.qihuang02.cyyn.network.CYYNMessages;
import cn.qihuang02.cyyn.network.packet.PlayAtSoundPacket;
import cn.qihuang02.cyyn.util.MentionParseResult;
import cn.qihuang02.cyyn.util.MentionParser;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
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

        handleItemMentions(event, sender);

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

    private static void handleItemMentions(@NotNull ServerChatEvent event, @NotNull ServerPlayer sender) {
        Component originalComponent = event.getMessage();
        String rawMessage = originalComponent.getString();
        if (rawMessage.isEmpty()) {
            return;
        }

        Style baseStyle = originalComponent.getStyle();
        ItemStack mainHandItem = sender.getMainHandItem();
        MutableComponent rebuilt = Component.empty();
        if (!baseStyle.isEmpty()) {
            rebuilt.setStyle(baseStyle);
        }

        boolean replacedAny = false;
        boolean warnedEmpty = false;
        int index = 0;

        while (index < rawMessage.length()) {
            int atIndex = rawMessage.indexOf('@', index);
            if (atIndex == -1) {
                break;
            }

            appendStyledLiteral(rebuilt, rawMessage.substring(index, atIndex), baseStyle);

            if (atIndex + 1 >= rawMessage.length()) {
                appendStyledLiteral(rebuilt, "@", baseStyle);
                index = atIndex + 1;
                continue;
            }

            if (atIndex > 0 && isMentionChar(rawMessage.charAt(atIndex - 1))) {
                appendStyledLiteral(rebuilt, "@", baseStyle);
                index = atIndex + 1;
                continue;
            }

            int tokenEnd = atIndex + 1;
            while (tokenEnd < rawMessage.length() && isMentionChar(rawMessage.charAt(tokenEnd))) {
                tokenEnd++;
            }

            if (tokenEnd == atIndex + 1) {
                appendStyledLiteral(rebuilt, "@", baseStyle);
                index = tokenEnd;
                continue;
            }

            String token = rawMessage.substring(atIndex + 1, tokenEnd);
            if ("item".equalsIgnoreCase(token)) {
                if (mainHandItem.isEmpty()) {
                    appendStyledLiteral(rebuilt, rawMessage.substring(atIndex, tokenEnd), baseStyle);
                    if (!warnedEmpty) {
                        sender.sendSystemMessage(
                                Component.translatable("message.cyyn.item.empty").withStyle(ChatFormatting.RED)
                        );
                        warnedEmpty = true;
                    }
                } else {
                    MutableComponent itemComponent = createItemComponent(mainHandItem);
                    rebuilt.append(itemComponent);
                    replacedAny = true;
                }
            } else {
                appendStyledLiteral(rebuilt, rawMessage.substring(atIndex, tokenEnd), baseStyle);
            }

            index = tokenEnd;
        }

        if (index < rawMessage.length()) {
            appendStyledLiteral(rebuilt, rawMessage.substring(index), baseStyle);
        }

        if (replacedAny) {
            event.setMessage(rebuilt);
        }
    }

    private static boolean isMentionChar(char ch) {
        return Character.isLetterOrDigit(ch) || ch == '_';
    }

    @NotNull
    private static MutableComponent createItemComponent(@NotNull ItemStack stack) {
        MutableComponent itemName = ComponentUtils.wrapInSquareBrackets(stack.getHoverName().copy());
        HoverEvent hoverEvent = new HoverEvent(
                HoverEvent.Action.SHOW_ITEM,
                new HoverEvent.ItemStackInfo(stack.copy())
        );

        return itemName.withStyle(style -> style
                .withHoverEvent(hoverEvent)
                .withColor(stack.getRarity().color)
                .withInsertion(stack.getDescriptionId())
        );
    }

    private static void appendStyledLiteral(@NotNull MutableComponent builder, @NotNull String text, @NotNull Style baseStyle) {
        if (text.isEmpty()) {
            return;
        }
        MutableComponent literal = Component.literal(text);
        if (!baseStyle.isEmpty()) {
            literal.setStyle(baseStyle);
        }
        builder.append(literal);
    }
}
