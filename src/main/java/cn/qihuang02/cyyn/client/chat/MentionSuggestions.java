package cn.qihuang02.cyyn.client.chat;

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

    public void refresh() {
        String value = this.input.getValue();
        int cursor = this.input.getCursorPosition();
        if (cursor < 0 || cursor > value.length()) {
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

    private static boolean isMentionChar(char ch) {
        return Character.isLetterOrDigit(ch) || ch == '_';
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
    private @NotNull Suggestions buildSuggestions(String typed) {
        ClientPacketListener connection = this.minecraft.getConnection();

        LocalPlayer player = this.minecraft.player;

        TreeSet<String> merged = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        merged.addAll(ClientMentionGroupTokens.getTokens());
        merged.add("here");
        merged.add("near");
        merged.add("item");

        if (connection != null) {
            connection.getOnlinePlayers().stream()
                    .map(PlayerInfo::getProfile)
                    .filter(Objects::nonNull)
                    .filter(profile -> player == null || !profile.getId().equals(player.getUUID()))
                    .map(profile -> profile.getName() == null ? "" : profile.getName())
                    .filter(name -> !name.isEmpty())
                    .forEach(merged::add);
        }

        StringRange range = StringRange.between(this.replaceStart, this.replaceEnd);
        if (merged.isEmpty()) {
            return new Suggestions(range, Collections.emptyList());
        }

        String lowerTyped = typed.toLowerCase(Locale.ROOT);
        List<Suggestion> list = new ArrayList<>();
        for (String candidate : merged) {
            if (lowerTyped.isEmpty() || candidate.toLowerCase(Locale.ROOT).startsWith(lowerTyped)) {
                list.add(new Suggestion(range, candidate));
            }
        }
        if (list.isEmpty()) {
            return new Suggestions(range, Collections.emptyList());
        }
        return new Suggestions(range, list);
    }
}
