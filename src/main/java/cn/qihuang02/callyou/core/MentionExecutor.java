package cn.qihuang02.callyou.core;

import cn.qihuang02.callyou.api.*;
import cn.qihuang02.callyou.api.event.MentionEvent;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.ServerChatEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class MentionExecutor {

    private static final MentionGuard GUARD = new MentionGuard();

    public static void handleChatEvent(@NotNull ServerChatEvent event) {
        ServerPlayer sender = event.getPlayer();

        try {
            String raw = event.getRawText();
            Component originalMessage = event.getMessage();

            List<MentionResolver.ResolvedMention> mentions =
                    MentionResolver.resolve(sender.server, sender, raw);
            if (mentions.isEmpty()) {
                return;
            }

            int effectiveMentionCount = 0;

            for (MentionResolver.ResolvedMention parsed : mentions) {
                MentionType type = parsed.mentionType();
                if (type == null) {
                    continue;
                }
                effectiveMentionCount++;

                if (!GUARD.canUseMentionType(sender, type)) {
                    String key = parsed.key();
                    Component display = Component.literal("@" + key);
                    sender.sendSystemMessage(
                            Component.translatable("message.callyou.no_permission", display)
                    );
                    event.setCanceled(true);
                    return;
                }
            }

            if (effectiveMentionCount <= 0) {
                return;
            }

            long nowTick = sender.server.getTickCount();

            if (!GUARD.checkMessageRate(sender, effectiveMentionCount, nowTick)) {
                sender.sendSystemMessage(
                        Component.translatable("message.callyou.too_many_mentions")
                );
                event.setCanceled(true);
                return;
            }

            MutableComponent rebuilt = Component.literal("");
            int lastIndex = 0;

            List<ServerPlayer> allTargetsHit = new ArrayList<>();

            for (MentionResolver.ResolvedMention parsed : mentions) {
                int start = parsed.startIndex();
                int end = parsed.endIndex();

                if (start < lastIndex || start >= raw.length() || end <= start) {
                    continue;
                }

                if (start > lastIndex) {
                    String before = raw.substring(lastIndex, start);
                    if (!before.isEmpty()) {
                        rebuilt.append(before);
                    }
                }

                MentionType type = parsed.mentionType();
                if (type == null) {
                    String literal = raw.substring(start, Math.min(end, raw.length()));
                    rebuilt.append(literal);
                    lastIndex = end;
                    continue;
                }

                MentionContext context = new MentionContext(
                        sender,
                        originalMessage,
                        raw,
                        parsed.key()
                );

                TextFormatter formatter = type.textFormatter();

                Component formattedMention = formatter.format(context);

                if (formatter.supportReply()) {
                    String suggestion = formatter.buildReplySuggestion(context, formattedMention);
                    if (!suggestion.isBlank()) {
                        formattedMention = formattedMention.copy().withStyle(style ->
                                style.withClickEvent(
                                        new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, suggestion)
                                )
                        );
                    }
                }

                rebuilt.append(formattedMention);

                List<ServerPlayer> hit = executeSingleMention(type, context, nowTick);
                allTargetsHit.addAll(hit);

                lastIndex = end;
            }

            if (lastIndex < raw.length()) {
                String tail = raw.substring(lastIndex);
                if (!tail.isEmpty()) {
                    rebuilt.append(tail);
                }
            }

            GUARD.recordUsage(sender, allTargetsHit, nowTick);

            event.setMessage(rebuilt);
        } catch (MentionCancelException cancel) {
            sender.sendSystemMessage(cancel.getReason());
            event.setCanceled(true);
        }
    }

    private static @NotNull List<ServerPlayer> executeSingleMention(@NotNull MentionType type,
                                                                    @NotNull MentionContext context,
                                                                    long nowTick) {
        TargetProvider targetProvider = type.targetProvider();
        NotificationRule notificationRule = type.notificationRule();

        List<ServerPlayer> rawTargets = new ArrayList<>(targetProvider.getTargets(context));

        MentionEvent.Pre preEvent = new MentionEvent.Pre(context, type, rawTargets);
        NeoForge.EVENT_BUS.post(preEvent);

        if (preEvent.isCanceled() || preEvent.getTargets().isEmpty()) {
            return List.of();
        }

        List<ServerPlayer> preFiltered = new ArrayList<>(preEvent.getTargets());

        List<ServerPlayer> filteredTargets =
                GUARD.filterTargets(context.sender(), type, context, preFiltered, nowTick);

        if (filteredTargets.isEmpty()) {
            return List.of();
        }

        notificationRule.apply(context, filteredTargets);

        NeoForge.EVENT_BUS.post(new MentionEvent.Post(context, type, filteredTargets));

        return filteredTargets;
    }
}
