package cn.qihuang02.cyyn.client.chat;

import cn.qihuang02.cyyn.util.mention.MentionTextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class MentionHighlighter {
    private static final Style PLAYER_STYLE = ClientMentionContext.styleFor(ChatFormatting.AQUA);
    private static final Style DEFAULT_GROUP_STYLE = ClientMentionContext.styleFor(ChatFormatting.LIGHT_PURPLE);
    private final Minecraft minecraft;
    private final MentionCandidateProvider candidateProvider;
    private final ClientMentionContext mentionContext;
    private boolean dirty = true;
    private String cachedValue = "";
    private int cachedRevision = -1;
    private FormattedCharSequence cachedSequence = Component.literal("").getVisualOrderText();

    public MentionHighlighter(@NotNull Minecraft minecraft) {
        this.minecraft = minecraft;
        this.candidateProvider = new MentionCandidateProvider(minecraft);
        this.mentionContext = ClientMentionContext.getInstance();
    }

    public static @NotNull FormattedCharSequence vanillaFormatter(String value, int cursorPosition) {
        return Component.literal(value == null ? "" : value).getVisualOrderText();
    }

    private static String normalize(@NotNull String name) {
        return MentionTextUtils.normalizeToken(name).toLowerCase(Locale.ROOT);
    }

    private static Style resolveStyle(@NotNull String name,
                                      @NotNull Set<String> groupNames,
                                      Set<String> playerNames,
                                      @NotNull Map<String, Style> groupStyles,
                                      @NotNull Map<String, Style> functionStyles) {
        String lowered = name.toLowerCase(Locale.ROOT);
        Style functionStyle = functionStyles.get(lowered);
        if (functionStyle != null) {
            return functionStyle;
        }
        if (groupNames.contains(lowered)) {
            return groupStyles.getOrDefault(lowered, DEFAULT_GROUP_STYLE);
        }
        if (playerNames.contains(lowered)) {
            return PLAYER_STYLE;
        }
        return Style.EMPTY;
    }

    public void invalidate() {
        this.dirty = true;
    }

    public @NotNull FormattedCharSequence format(String value, int cursorPosition) {
        String sanitized = value == null ? "" : value;
        int revision = ClientMentionGroupNames.getRevision();
        int registryVersion = this.mentionContext.getRegistryVersion();
        int cachedContextVersion = -1;
        if (!this.dirty && this.cachedRevision == revision && cachedContextVersion == registryVersion && sanitized.equals(this.cachedValue)) {
            return this.cachedSequence;
        }

        FormattedCharSequence sequence = highlightMentions(sanitized);
        this.cachedValue = sanitized;
        this.cachedRevision = revision;
        this.cachedSequence = sequence;
        this.dirty = false;
        return sequence;
    }

    private @NotNull FormattedCharSequence highlightMentions(@NotNull String value) {
        if (value.isEmpty()) {
            return Component.literal("").getVisualOrderText();
        }

        LocalPlayer localPlayer = this.minecraft.player;
        ClientPacketListener connection = this.minecraft.getConnection();

        Set<String> groupNames = this.mentionContext.getNormalizedGroupNames();
        Set<String> playerNames = this.candidateProvider.getPlayerNames(connection, localPlayer);
        Map<String, Style> groupStyles = this.mentionContext.getGroupStyles();
        Map<String, Style> functionStyles = this.mentionContext.getFunctionStyles();

        MutableComponent builder = Component.empty();
        boolean changed = false;
        int index = 0;

        for (MentionTextUtils.MentionTokenRange range : MentionTextUtils.scanMentions(value)) {
            if (range.mentionStart() > index) {
                builder.append(Component.literal(value.substring(index, range.mentionStart())));
            }

            String name = range.tokenIn(value);
            Style style = resolveStyle(name, groupNames, playerNames, groupStyles, functionStyles);
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
