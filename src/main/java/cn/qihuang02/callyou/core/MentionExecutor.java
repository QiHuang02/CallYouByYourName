package cn.qihuang02.callyou.core;

import cn.qihuang02.callyou.api.MentionType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.ServerChatEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class MentionExecutor {
    private static final MentionGuard GUARD = new MentionGuard();

    public static void handleChatEvent(@NotNull ServerChatEvent event) {
        ServerPlayer sender = event.getPlayer();
        String raw = event.getRawText();
        Component originalMessage = event.getMessage();

        List<MentionResolver.ResolvedMention> mentions =
                MentionResolver.resolve(sender.server, sender, raw);
        if (mentions.isEmpty()) {
            return;
        }

        for (MentionResolver.ResolvedMention parsed : mentions) {
            MentionType type = parsed.mentionType();
            if (type == null) {
                continue;
            }


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


        MutableComponent rebuilt = Component.literal("");
        int lastIndex = 0;


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
            }
        }
    }
}
