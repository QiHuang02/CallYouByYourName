package cn.qihuang02.cyyn.client.chat;

import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public final class MentionSuggestions {
    private static final int MAX_VISIBLE = 10;

    private final Minecraft minecraft;
    private final Font font;
    private EditBox input;
    private Suggestions suggestions;
    private int selection;
    private boolean active;
    private int replaceStart;
    private int replaceEnd;
    private Rect2i area;
    private int mentionTokenStart;
    private int entryHeight;
    private int displayOffset;
    private int visibleEntries;

    public MentionSuggestions(@NotNull Minecraft minecraft) {
        this.minecraft = minecraft;
        this.font = minecraft.font;
        this.suggestions = new Suggestions(StringRange.at(0), Collections.emptyList());
        this.area = new Rect2i(0, 0, 0, 0);
        this.mentionTokenStart = -1;
    }

    private static boolean isMentionChar(char ch) {
        return Character.isLetterOrDigit(ch) || ch == '_';
    }

    public void attach(EditBox input) {
        this.input = input;
    }

    public void hide() {
        this.active = false;
        this.suggestions = new Suggestions(StringRange.at(0), Collections.emptyList());
        this.selection = 0;
        this.displayOffset = 0;
        this.visibleEntries = 0;
        this.entryHeight = 0;
        this.mentionTokenStart = -1;
    }

    public void refresh() {
        if (this.input == null) {
            hide();
            return;
        }

        String value = this.input.getValue();
        int typedCursor = this.input.getCursorPosition();
        if (typedCursor < 0 || typedCursor > value.length()) {
            hide();
            return;
        }

        int tokenStart = findMentionStart(value, typedCursor);
        if (tokenStart == -1) {
            hide();
            return;
        }

        int tokenEnd = findMentionEnd(value, tokenStart + 1);
        this.replaceStart = tokenStart + 1;
        this.replaceEnd = tokenEnd;
        this.mentionTokenStart = tokenStart;

        if (typedCursor <= tokenStart || typedCursor > tokenEnd) {
            hide();
            return;
        }

        String typed = value.substring(this.replaceStart, typedCursor);
        Suggestions newSuggestions = buildSuggestions(typed);
        if (newSuggestions.getList().isEmpty()) {
            hide();
            return;
        }

        this.suggestions = newSuggestions;
        this.selection = Mth.clamp(this.selection, 0, this.suggestions.getList().size() - 1);
        this.active = true;
    }

    public boolean handleKeyPressed(int keyCode) {
        if (!isActive()) {
            return false;
        }

        if (keyCode == GLFW.GLFW_KEY_UP) {
            moveSelection(-1);
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_DOWN) {
            moveSelection(1);
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_TAB || keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            return completeSelection();
        }

        return false;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isActive()) {
            return false;
        }
        if (!this.area.contains((int) mouseX, (int) mouseY)) {
            return false;
        }
        if (button != 0) {
            return true;
        }

        int index = this.displayOffset + (int) ((mouseY - this.area.getY()) / this.entryHeight);
        if (index >= 0 && index < this.suggestions.getList().size()) {
            this.selection = index;
            return completeSelection();
        }
        return true;
    }

    public boolean mouseScrolled(double deltaY) {
        if (!isActive()) {
            return false;
        }
        if (deltaY > 0) {
            moveSelection(-1);
            return true;
        }
        if (deltaY < 0) {
            moveSelection(1);
            return true;
        }
        return false;
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, EditBox input) {
        if (!isActive() || this.input != input) {
            return;
        }

        List<Suggestion> list = this.suggestions.getList();
        if (list.isEmpty()) {
            return;
        }

        this.entryHeight = this.font.lineHeight + 4;
        int maxVisible = Math.min(MAX_VISIBLE, list.size());
        this.selection = Mth.clamp(this.selection, 0, list.size() - 1);
        this.displayOffset = Mth.clamp(this.selection - maxVisible + 1, 0, Math.max(0, list.size() - maxVisible));
        int visibleEnd = Math.min(list.size(), this.displayOffset + maxVisible);
        this.visibleEntries = visibleEnd - this.displayOffset;

        int width = 0;
        for (int i = this.displayOffset; i < visibleEnd; i++) {
            width = Math.max(width, this.font.width(list.get(i).getText()));
        }
        width += 8;

        int startX = calculateAnchorX(input);
        int startY = input.getY() - 2 - this.visibleEntries * this.entryHeight;
        if (startY < 0) {
            startY = input.getY() + input.getHeight() + 2;
        }

        this.area = new Rect2i(startX, startY, width, this.visibleEntries * this.entryHeight);

        guiGraphics.fill(startX - 1, startY - 1, startX + width + 1, startY + this.visibleEntries * this.entryHeight + 1, 0xC0101010);

        for (int idx = 0; idx < this.visibleEntries; idx++) {
            int suggestionIndex = this.displayOffset + idx;
            int y = startY + idx * this.entryHeight;
            int background = suggestionIndex == this.selection ? 0xFF2F2F2F : 0xFF000000;
            guiGraphics.fill(startX, y, startX + width, y + this.entryHeight, background);
            guiGraphics.drawString(this.font, list.get(suggestionIndex).getText(), startX + 4, y + 2, suggestionIndex == this.selection ? 0xFFFFFF : 0xFFAAAAAA, false);
        }
    }

    private int calculateAnchorX(@NotNull EditBox input) {
        int anchorX = input.getX();
        if (this.mentionTokenStart < 0 || this.mentionTokenStart > input.getValue().length()) {
            return anchorX;
        }

        int candidate = input.getScreenX(this.mentionTokenStart);
        int minX = 0;
        int maxX = this.minecraft.getWindow().getGuiScaledWidth();
        if (candidate < minX || candidate > maxX) {
            return anchorX;
        }

        return candidate;
    }

    private boolean isActive() {
        return this.active && !this.suggestions.getList().isEmpty();
    }

    private void moveSelection(int delta) {
        int size = this.suggestions.getList().size();
        this.selection = Mth.clamp(this.selection + delta, 0, size - 1);
    }

    private boolean completeSelection() {
        if (!isActive()) {
            return false;
        }

        Suggestion suggestion = this.suggestions.getList().get(this.selection);
        applySuggestion(suggestion.getText());
        return true;
    }

    private void applySuggestion(String suggestion) {
        if (this.input == null) {
            return;
        }
        String value = this.input.getValue();
        String before = value.substring(0, this.replaceStart);
        String after = value.substring(this.replaceEnd);
        String newValue = before + suggestion + after;
        this.input.setValue(newValue);
        int cursorPos = before.length() + suggestion.length();
        this.input.setCursorPosition(cursorPos);
        this.input.setHighlightPos(cursorPos);
        hide();
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
