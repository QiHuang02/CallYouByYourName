package cn.qihuang02.cyyn.client.chat;

import cn.qihuang02.cyyn.api.mention.MentionFunction;
import cn.qihuang02.cyyn.api.mention.MentionGroup;
import cn.qihuang02.cyyn.api.mention.MentionRegistry;
import cn.qihuang02.cyyn.common.mention.MentionTextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class MentionHighlighter {
    private static final Style PLAYER_STYLE = styleFor(ChatFormatting.AQUA);
    private static final Style DEFAULT_GROUP_STYLE = styleFor(ChatFormatting.LIGHT_PURPLE);

    private static @NotNull Style styleFor(@NotNull ChatFormatting color) {
        return Style.EMPTY.withColor(color);
    }

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
        if (!this.dirty && this.cachedRevision == revision && sanitized.equals(this.cachedValue)) {
            return this.cachedSequence;
        }

        FormattedCharSequence sequence = highlightMentions(sanitized, ClientMentionGroupNames.getNames());
        this.cachedValue = sanitized;
        this.cachedRevision = revision;
        this.cachedSequence = sequence;
        this.dirty = false;
        return sequence;
    }

    private @NotNull FormattedCharSequence highlightMentions(@NotNull String value, List<String> groupNameList) {
        if (value.isEmpty()) {
            return Component.literal("").getVisualOrderText();
        }

        LocalPlayer localPlayer = this.minecraft.player;
        ClientPacketListener connection = this.minecraft.getConnection();

        Set<String> groupNames = this.candidateProvider.getGroupNames(groupNameList, localPlayer);
        Set<String> playerNames = this.candidateProvider.getPlayerNames(connection, localPlayer);

        Map<String, Style> groupStyles = new HashMap<>();
        MentionRegistry.streamGroups().forEach(group -> {
            String groupName = group.name();
            if (groupName == null || groupName.isEmpty()) {
                return;
            }
            groupStyles.put(normalize(groupName), styleFor(group.pointColor()));
        });

        Map<String, Style> functionStyles = new HashMap<>();
        MentionRegistry.streamFunctions().forEach(function -> {
            String functionName = function.name();
            if (functionName == null || functionName.isEmpty()) {
                return;
            }
            functionStyles.put(normalize(functionName), styleFor(function.pointColor()));
        });

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
