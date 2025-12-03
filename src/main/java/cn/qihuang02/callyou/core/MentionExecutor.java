package cn.qihuang02.callyou.core;

import cn.qihuang02.callyou.api.MentionContext;
import cn.qihuang02.callyou.api.MentionType;
import cn.qihuang02.callyou.api.NotificationRule;
import cn.qihuang02.callyou.api.TargetProvider;
import cn.qihuang02.callyou.api.event.MentionEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.ServerChatEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class MentionExecutor {
    public static void handleChatEvent(@NotNull ServerChatEvent event) {
        ServerPlayer sender = event.getPlayer();
        String raw = event.getRawText();
        Component originalMessage = event.getMessage();

        List<MentionResolver.ParsedMention> mentions = MentionResolver.findAll(sender, raw);
        if (mentions.isEmpty()) {
            return;
        }

        MutableComponent rebuilt = Component.literal("");
        int lastIndex = 0;

        for (MentionResolver.ParsedMention parsed : mentions) {
            int start = parsed.startIndex();
            int end = parsed.endIndex();
            MentionType type = parsed.type();

            if (start > lastIndex) {
                String before = raw.substring(lastIndex, start);
                if (!before.isEmpty()) {
                    rebuilt.append(before);
                }
            }

            MentionContext ctx = new MentionContext(
                    sender,
                    originalMessage,
                    raw,
                    parsed.key()
            );

            Component formattedMention = type.textFormatter().format(ctx);
            rebuilt.append(formattedMention);

            executeSingleMention(type, ctx);

            lastIndex = end;
        }

        if (lastIndex < raw.length()) {
            String tail = raw.substring(lastIndex);
            if (!tail.isEmpty()) {
                rebuilt.append(tail);
            }
        }

        event.setMessage(rebuilt);
    }

    private static void executeSingleMention(@NotNull MentionType type, @NotNull MentionContext context) {
        TargetProvider targetProvider = type.targetProvider();
        NotificationRule notificationRule = type.notificationRule();

        List<ServerPlayer> targets = new ArrayList<>(targetProvider.getTargets(context));
        if (targets.isEmpty()) {
            return;
        }

        MentionEvent.Pre preEvent = NeoForge.EVENT_BUS.post(
                new MentionEvent.Pre(context, type, targets)
        );

        if (preEvent.isCanceled() || preEvent.getTargets().isEmpty()) {
            return;
        }

        List<ServerPlayer> finalTargets = preEvent.getTargets();

        notificationRule.apply(context, targets);

        NeoForge.EVENT_BUS.post(
                new MentionEvent.Post(context, type, finalTargets)
        );
    }
}
