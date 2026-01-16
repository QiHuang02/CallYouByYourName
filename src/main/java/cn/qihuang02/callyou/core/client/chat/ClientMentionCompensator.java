package cn.qihuang02.callyou.core.client.chat;

import cn.qihuang02.callyou.api.client.ClientMentionMetadata;
import cn.qihuang02.callyou.util.MentionKeyUtils;
import cn.qihuang02.callyou.core.MentionTokens;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class ClientMentionCompensator {
    private static final String ITEM_KEY = "item";
    private static final String SPOT_KEY = "spot";

    public static @NotNull Component compensate(
            @NotNull Minecraft minecraft,
            @NotNull Component message
    ) {
        ClientMentionMetadata context = ClientMentionMetadata.ClientMentionMetadataCache.get(minecraft);
        if (message.getString().indexOf('@') < 0) {
            return message;
        }
        RewriteResult result = rewriteComponent(message, context);
        return result.changed() ? result.component() : message;
    }

    private static @NotNull RewriteResult rewriteComponent(
            @NotNull Component input,
            @NotNull ClientMentionMetadata context
    ) {
        RewriteResult base = rewriteContents(input, context);
        List<Component> siblings = input.getSiblings();
        if (siblings.isEmpty()) {
            return base;
        }

        boolean siblingChanged = false;
        List<Component> rebuiltSiblings = new ArrayList<>(siblings.size());
        for (Component sibling : siblings) {
            RewriteResult result = rewriteComponent(sibling, context);
            rebuiltSiblings.add(result.component());
            siblingChanged |= result.changed();
        }

        if (!base.changed() && !siblingChanged) {
            return base;
        }

        MutableComponent rebuilt;
        if (base.changed() && base.component() instanceof MutableComponent mutable) {
            rebuilt = mutable.copy();
        } else {
            rebuilt = MutableComponent.create(base.component().getContents());
            rebuilt.setStyle(base.component().getStyle());
        }

        for (Component sibling : rebuiltSiblings) {
            rebuilt.append(sibling);
        }

        return new RewriteResult(rebuilt, true);
    }

    private static @NotNull RewriteResult rewriteContents(
            @NotNull Component input,
            @NotNull ClientMentionMetadata context
    ) {
        if (input.getContents() instanceof PlainTextContents plain) {
            return rewritePlainText(input, plain.text(), context);
        }
        if (input.getContents() instanceof TranslatableContents translatable) {
            Object[] args = translatable.getArgs();
            Object[] rebuiltArgs = args.clone();
            boolean changed = false;

            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                if (arg instanceof Component component) {
                    RewriteResult result = rewriteComponent(component, context);
                    rebuiltArgs[i] = result.component();
                    changed |= result.changed();
                }
            }

            if (!changed) {
                return new RewriteResult(input, false);
            }

            String fallback = translatable.getFallback();
            MutableComponent rebuilt = fallback == null
                    ? Component.translatable(translatable.getKey(), rebuiltArgs)
                    : Component.translatableWithFallback(translatable.getKey(), fallback, rebuiltArgs);
            rebuilt.setStyle(input.getStyle());
            return new RewriteResult(rebuilt, true);
        }
        return new RewriteResult(input, false);
    }

    @Contract("_, _, _ -> new")
    private static @NotNull RewriteResult rewritePlainText(
            @NotNull Component input,
            @NotNull String text,
            @NotNull ClientMentionMetadata context
    ) {
        if (text.indexOf('@') < 0) {
            return new RewriteResult(input, false);
        }

        List<MentionTokens.Token> tokens = MentionTokens.scan(text);
        if (tokens.isEmpty()) {
            return new RewriteResult(input, false);
        }

        Style baseStyle = input.getStyle();
        MutableComponent rebuilt = Component.empty();
        int lastIndex = 0;
        boolean changed = false;

        for (MentionTokens.Token token : tokens) {
            int start = token.startIndex();
            int end = token.endIndex();

            if (start < lastIndex || start >= text.length() || end <= start) {
                continue;
            }

            if (start > lastIndex) {
                rebuilt.append(Component.literal(text.substring(lastIndex, start)).setStyle(baseStyle));
            }

            Component replacement = replacementForToken(token.key());
            if (replacement != null) {
                Style mergedStyle = mergeStyle(baseStyle, token.key(), context);
                rebuilt.append(replacement.copy().setStyle(mergedStyle));
                changed = true;
            } else {
                rebuilt.append(Component.literal(text.substring(start, Math.min(end, text.length())))
                        .setStyle(baseStyle));
            }

            lastIndex = end;
        }

        if (lastIndex < text.length()) {
            rebuilt.append(Component.literal(text.substring(lastIndex)).setStyle(baseStyle));
        }

        if (!changed) {
            return new RewriteResult(input, false);
        }

        return new RewriteResult(rebuilt, true);
    }

    private static @NotNull Style mergeStyle(
            @NotNull Style baseStyle,
            @NotNull String key,
            @NotNull ClientMentionMetadata context
    ) {
        return context.findStyle(key).map(style -> style.applyTo(baseStyle)).orElse(baseStyle);
    }

    private static @Nullable Component replacementForToken(@NotNull String key) {
        String normalized = MentionKeyUtils.normalize(key);
        if (ITEM_KEY.equals(normalized)) {
            return Component.translatable("message.callyou.item.label");
        }
        if (SPOT_KEY.equals(normalized)) {
            return Component.translatable("message.callyou.spot.label");
        }
        return null;
    }

    private record RewriteResult(@NotNull Component component, boolean changed) {
    }
}
