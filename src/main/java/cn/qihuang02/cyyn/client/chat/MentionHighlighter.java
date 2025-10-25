package cn.qihuang02.cyyn.client.chat;

import com.mojang.authlib.GameProfile;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;


public class MentionHighlighter {
    private static final Style PLAYER_STYLE = Style.EMPTY.withColor(ChatFormatting.AQUA);
    private static final Style GROUP_STYLE = Style.EMPTY.withColor(ChatFormatting.LIGHT_PURPLE);
    private static final Style ITEM_STYLE = Style.EMPTY.withColor(ChatFormatting.GOLD);

    private final Minecraft minecraft;

    private boolean dirty = true;
    private String cachedValue = "";
    private int cachedRevision = -1;
    private FormattedCharSequence cachedSequence = Component.literal("").getVisualOrderText();

    public MentionHighlighter(@NotNull Minecraft minecraft) {
        this.minecraft = minecraft;
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

    private static boolean isMentionChar(char ch) {
        return Character.isLetterOrDigit(ch) || ch == '_';
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

        Set<String> groupTokens = collectGroupTokens(groupTokenList);
        Set<String> playerTokens = collectPlayerTokens();

        MutableComponent builder = Component.empty();
        boolean changed = false;
        int index = 0;

        while (index < value.length()) {
            int atIndex = value.indexOf('@', index);
            if (atIndex == -1) {
                break;
            }

            if (atIndex > index) {
                builder.append(Component.literal(value.substring(index, atIndex)));
            }

            int tokenEnd = atIndex + 1;
            while (tokenEnd < value.length() && isMentionChar(value.charAt(tokenEnd))) {
                tokenEnd++;
            }

            if (tokenEnd == atIndex + 1) {
                builder.append(Component.literal("@"));
                index = tokenEnd;
                continue;
            }

            if (atIndex > 0 && isMentionChar(value.charAt(atIndex - 1))) {
                builder.append(Component.literal(value.substring(atIndex, tokenEnd)));
                index = tokenEnd;
                continue;
            }

            String token = value.substring(atIndex + 1, tokenEnd);
            Style style = resolveStyle(token, groupTokens, playerTokens);
            if (style.isEmpty()) {
                builder.append(Component.literal(value.substring(atIndex, tokenEnd)));
            } else {
                builder.append(Component.literal(value.substring(atIndex, tokenEnd)).withStyle(style));
                changed = true;
            }

            index = tokenEnd;
        }

        if (index < value.length()) {
            builder.append(Component.literal(value.substring(index)));
        }

        return changed ? builder.getVisualOrderText() : Component.literal(value).getVisualOrderText();
    }

    private @NotNull Set<String> collectGroupTokens(@NotNull List<String> tokens) {
        Set<String> normalized = new HashSet<>();
        for (String token : tokens) {
            if (token == null || token.isEmpty()) {
                continue;
            }
            normalized.add(token.toLowerCase(Locale.ROOT));
        }
        normalized.add("here");
        normalized.add("near");
        return normalized;
    }

    private @NotNull Set<String> collectPlayerTokens() {
        ClientPacketListener connection = this.minecraft.getConnection();
        if (connection == null) {
            return Set.of();
        }

        LocalPlayer localPlayer = this.minecraft.player;
        Set<String> names = new HashSet<>();

        for (PlayerInfo info : connection.getOnlinePlayers()) {
            GameProfile profile = info.getProfile();
            if (profile == null) {
                continue;
            }

            if (localPlayer != null && profile.getId() != null && profile.getId().equals(localPlayer.getUUID())) {
                continue;
            }

            String name = profile.getName();
            if (name != null && !name.isEmpty()) {
                names.add(name.toLowerCase(Locale.ROOT));
            }
        }

        return names;
    }
}
