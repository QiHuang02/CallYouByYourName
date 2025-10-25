package cn.qihuang02.cyyn.client.chat;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class MentionSuggestions extends CommandSuggestions {
    private static final int MAX_VISIBLE = 10;
    private static final int BACKGROUND_COLOR = 0xC0101010;

    private final Minecraft minecraft;
    private final EditBox input;
    private int replaceStart;
    private int replaceEnd;

    public MentionSuggestions(@NotNull Minecraft minecraft, @NotNull Screen screen, @NotNull EditBox input) {
        super(minecraft, screen, input, minecraft.font, false, false, 0, MAX_VISIBLE, true, BACKGROUND_COLOR);
        this.minecraft = minecraft;
        this.input = input;
    }

    private static boolean isMentionChar(char ch) {
        return Character.isLetterOrDigit(ch) || ch == '_';
    }

    private static void addMatchingCandidates(@NotNull List<String> candidates, @NotNull String lowerTyped,
                                              @NotNull StringRange range, @NotNull List<Suggestion> output,
                                              @NotNull Set<String> seen) {
        for (String candidate : candidates) {
            String loweredCandidate = candidate.toLowerCase(Locale.ROOT);
            if (!lowerTyped.isEmpty() && !loweredCandidate.startsWith(lowerTyped)) {
                continue;
            }
            if (!seen.add(loweredCandidate)) {
                continue;
            }
            output.add(new Suggestion(range, candidate));
        }
    }

    private static @NotNull List<String> collectGroupCandidates(LocalPlayer player) {
        List<String> candidates = new ArrayList<>();
        for (String token : ClientMentionGroupTokens.getTokens()) {
            if (token != null && !token.isEmpty()) {
                candidates.add(token);
            }
        }
        candidates.add("here");
        candidates.add("near");
        if (player != null && !player.getMainHandItem().isEmpty()) {
            candidates.add("item");
        }

        return sortAndDeduplicate(candidates);
    }

    private static @NotNull List<String> collectPlayerCandidates(ClientPacketListener connection, LocalPlayer localPlayer) {
        if (connection == null) {
            return Collections.emptyList();
        }

        List<String> names = new ArrayList<>();
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
                names.add(name);
            }
        }

        return sortAndDeduplicate(names);
    }

    private static @NotNull List<String> sortAndDeduplicate(@NotNull List<String> values) {
        if (values.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, String> deduplicated = new LinkedHashMap<>();
        for (String value : values) {
            if (value == null) {
                continue;
            }
            String trimmed = value.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            deduplicated.putIfAbsent(trimmed.toLowerCase(Locale.ROOT), trimmed);
        }

        List<String> sorted = new ArrayList<>(deduplicated.values());
        sorted.sort(String.CASE_INSENSITIVE_ORDER);
        return sorted;
    }

    public void refresh() {
        String value = this.input.getValue();
        int cursor = this.input.getCursorPosition();
        if (cursor < 0 || cursor > value.length()) {
            return;
        }

        if (isCommandInput(value)) {
            hide();
            return;
        }

        int tokenStart = findMentionStart(value, cursor);
        if (tokenStart == -1) {
            hide();
            return;
        }

        int tokenEnd = findMentionEnd(value, tokenStart + 1);
        if (cursor <= tokenStart || cursor > tokenEnd) {
            hide();
            return;
        }

        this.replaceStart = tokenStart + 1;
        this.replaceEnd = tokenEnd;

        String typed = value.substring(this.replaceStart, cursor);
        Suggestions suggestions = buildSuggestions(typed);
        if (suggestions.getList().isEmpty()) {
            hide();
            return;
        }

        this.pendingSuggestions = CompletableFuture.completedFuture(suggestions);
        this.showSuggestions(false);
    }

    @Override
    public void hide() {
        super.hide();
        this.pendingSuggestions = null;
    }

    public boolean handleKeyPressed(int keyCode) {
        if (isCommandInput(this.input.getValue())) {
            return false;
        }
        return super.keyPressed(keyCode, 0, 0);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean mouseScrolled(double deltaY) {
        return super.mouseScrolled(deltaY);
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, EditBox input) {
        if (this.input != input) {
            return;
        }

        super.render(guiGraphics, mouseX, mouseY);
    }

    private static boolean isCommandInput(@NotNull String value) {
        return !value.isEmpty() && value.charAt(0) == '/';
    }

    private int findMentionStart(@NotNull String value, int cursor) {
        int index = Math.min(cursor, value.length());
        while (index > 0 && !Character.isWhitespace(value.charAt(index - 1))) {
            index--;
        }
        if (index < value.length() && value.charAt(index) == '@') {
            if (index > 0 && isMentionChar(value.charAt(index - 1))) {
                return -1;
            }
            return index;
        }
        return -1;
    }

    private int findMentionEnd(@NotNull String value, int index) {
        int end = index;
        while (end < value.length() && isMentionChar(value.charAt(end))) {
            end++;
        }
        return end;
    }

    @Contract("_ -> new")
    private @NotNull Suggestions buildSuggestions(@NotNull String typed) {
        ClientPacketListener connection = this.minecraft.getConnection();

        LocalPlayer player = this.minecraft.player;

        StringRange range = StringRange.between(this.replaceStart, this.replaceEnd);
        String lowerTyped = typed.toLowerCase(Locale.ROOT);
        List<String> groupCandidates = collectGroupCandidates(player);
        List<String> playerCandidates = collectPlayerCandidates(connection, player);
        if (groupCandidates.isEmpty() && playerCandidates.isEmpty()) {
            return new Suggestions(range, Collections.emptyList());
        }

        List<Suggestion> list = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        addMatchingCandidates(groupCandidates, lowerTyped, range, list, seen);
        addMatchingCandidates(playerCandidates, lowerTyped, range, list, seen);

        if (list.isEmpty()) {
            return new Suggestions(range, Collections.emptyList());
        }
        return new Suggestions(range, list);
    }
}
