package cn.qihuang02.cyyn.client.chat;

import cn.qihuang02.cyyn.util.MentionCandidateProvider;
import cn.qihuang02.cyyn.util.MentionTextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public class MentionHighlighter {
    private static final Style PLAYER_STYLE = Style.EMPTY.withColor(ChatFormatting.AQUA);
    private static final Style GROUP_STYLE = Style.EMPTY.withColor(ChatFormatting.LIGHT_PURPLE);
    private static final Style ITEM_STYLE = Style.EMPTY.withColor(ChatFormatting.GOLD);

    private final Minecraft minecraft;
    private final MentionCandidateProvider candidateProvider;

    private boolean dirty = true;
    private String cachedValue = "";
    private int cachedRevision = -1;
    private FormattedCharSequence cachedSequence = Component.literal("").getVisualOrderText();

    public MentionHighlighter(@NotNull Minecraft minecraft) {
        this.minecraft = minecraft;
        this.candidateProvider = new MentionCandidateProvider(minecraft);
    }

    public static @NotNull FormattedCharSequence vanillaFormatter(String value, int cursorPosition) {
        return Component.literal(value == null ? "" : value).getVisualOrderText();
    }

    private static Style resolveStyle(@NotNull String token, @NotNull Set<String> groupTokens, Set<String> playerTokens) {
        String lowered = token.toLowerCase(Locale.ROOT);
        if ("item".equals(lowered)) {
            return ITEM_STYLE;
        }
        if (groupTokens.contains(lowered)) {
            return GROUP_STYLE;
        }
        if (playerTokens.contains(lowered)) {
            return PLAYER_STYLE;
        }
        return Style.EMPTY;
    }

    public void invalidate() {
        this.dirty = true;
    }

    public @NotNull FormattedCharSequence format(String value, int cursorPosition) {
        String sanitized = value == null ? "" : value;
        int revision = ClientMentionGroupTokens.getRevision();
        if (!this.dirty && this.cachedRevision == revision && sanitized.equals(this.cachedValue)) {
            return this.cachedSequence;
        }

        FormattedCharSequence sequence = highlightMentions(sanitized, ClientMentionGroupTokens.getTokens());
        this.cachedValue = sanitized;
        this.cachedRevision = revision;
        this.cachedSequence = sequence;
        this.dirty = false;
        return sequence;
    }

    private @NotNull FormattedCharSequence highlightMentions(@NotNull String value, List<String> groupTokenList) {
        if (value.isEmpty()) {
            return Component.literal("").getVisualOrderText();
        }

        LocalPlayer localPlayer = this.minecraft.player;
        ClientPacketListener connection = this.minecraft.getConnection();

        Set<String> groupTokens = this.candidateProvider.getGroupTokens(groupTokenList, localPlayer);
        Set<String> playerTokens = this.candidateProvider.getPlayerTokens(connection, localPlayer);

        MutableComponent builder = Component.empty();
        boolean changed = false;
        int index = 0;

        while (index < value.length()) {
            Optional<MentionTextUtils.MentionTokenRange> rangeOptional = MentionTextUtils.findTokenRange(value, index);
            if (rangeOptional.isEmpty()) {
                break;
            }

            MentionTextUtils.MentionTokenRange range = rangeOptional.get();
            if (range.mentionStart() > index) {
                builder.append(Component.literal(value.substring(index, range.mentionStart())));
            }

            String token = range.tokenIn(value);
            Style style = resolveStyle(token, groupTokens, playerTokens);
            String mentionText = range.mentionIn(value);
            if (style.isEmpty()) {
                builder.append(Component.literal(mentionText));
            } else {
                builder.append(Component.literal(mentionText).withStyle(style));
                changed = true;
            }

            index = range.tokenEnd();
        }

        if (index < value.length()) {
            builder.append(Component.literal(value.substring(index)));
        }

        return changed ? builder.getVisualOrderText() : Component.literal(value).getVisualOrderText();
    }
}
