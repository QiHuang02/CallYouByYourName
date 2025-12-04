package cn.qihuang02.callyou.client.chat;

import cn.qihuang02.callyou.core.MentionTokens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public final class MentionSuggestions extends CommandSuggestions {
    private static ClientMentionContext mentionContext;
    private static MentionCandidateProvider candidateProvider;

    private final Minecraft minecraft;
    private final ChatScreen screen;
    private final EditBox input;
    private final ClientMentionContext mentionContextInstance;
    private final MentionCandidateProvider candidateProviderInstance;
    private final List<String> suggestions = new ArrayList<>();
    private MentionTokens.Token currentToken;
    private int selection = -1;
    private boolean visible;
    private boolean dirty = true;
    private String lastValue = "";
    private int lastCursor = -1;

    private int boxLeft;
    private int boxTop;
    private int boxRight;
    private int boxBottom;
    private int lineHeight;

    private MentionSuggestions(@NotNull Minecraft minecraft,
                               @NotNull ChatScreen screen,
                               @NotNull EditBox input,
                               @NotNull ClientMentionContext mentionContext,
                               @NotNull MentionCandidateProvider candidateProvider,
                               @NotNull MentionHighlighter highlighter) {
        super(minecraft, screen, input, minecraft.font, false, false, 0, 7, true, Integer.MIN_VALUE);
        this.minecraft = minecraft;
        this.screen = screen;
        this.input = input;
        this.mentionContextInstance = mentionContext;
        this.candidateProviderInstance = candidateProvider;

        this.input.setFormatter(highlighter::format);
    }

    private static boolean isCommandInput(@NotNull String value) {
        return !value.isEmpty() && value.charAt(0) == '/';
    }

    private static MentionTokens.Token findTokenAtCursor(@NotNull String value, int cursor) {
        for (MentionTokens.Token token : MentionTokens.scan(value)) {
            if (cursor >= token.startIndex() + 1 && cursor <= token.endIndex()) {
                return token;
            }
        }

        if (cursor > 0 && value.charAt(cursor - 1) == '@') {
            return new MentionTokens.Token(cursor - 1, cursor, "");
        }

        return null;
    }

    private static void ensureProviders(@NotNull Minecraft minecraft) {
        if (mentionContext == null) {
            mentionContext = new ClientMentionContext(minecraft);
        }
        if (candidateProvider == null) {
            candidateProvider = new MentionCandidateProvider(minecraft);
        }
    }

    public static MentionSuggestions create(@NotNull Minecraft minecraft,
                                            @NotNull ChatScreen chatScreen,
                                            @NotNull EditBox input) {
        ensureProviders(minecraft);
        MentionHighlighter highlighter = new MentionHighlighter(minecraft, mentionContext, candidateProvider);
        return new MentionSuggestions(minecraft, chatScreen, input, mentionContext, candidateProvider, highlighter);
    }

    private void tick() {
        String value = input.getValue();
        int cursor = input.getCursorPosition();

        if (dirty || !Objects.equals(lastValue, value) || cursor != lastCursor) {
            updateSuggestions();
            lastValue = value;
            lastCursor = cursor;
            dirty = false;
        }
    }

    private void updateSuggestions() {
        String value = input.getValue();
        int cursor = input.getCursorPosition();

        if (cursor < 0 || cursor > value.length()) {
            clearSuggestions();
            return;
        }

        if (isCommandInput(value)) {
            clearSuggestions();
            return;
        }

        MentionTokens.Token token = findTokenAtCursor(value, cursor);

        if (token == null) {
            clearSuggestions();
            return;
        }

        this.currentToken = token;
        buildSuggestions(value, cursor);
    }

    private void buildSuggestions(@NotNull String value, int cursor) {
        int start = currentToken.startIndex() + 1;
        int end = Math.min(currentToken.endIndex(), cursor);
        if (cursor < start) {
            clearSuggestions();
            return;
        }

        String prefix = value.substring(start, end);
        String lowered = prefix.toLowerCase(Locale.ROOT);

        Set<String> candidates = new HashSet<>();
        candidates.addAll(mentionContextInstance.getMentionTypeKeys());
        candidates.addAll(candidateProviderInstance.getPlayerCandidates());

        suggestions.clear();
        for (String candidate : candidates) {
            if (candidate.toLowerCase(Locale.ROOT).startsWith(lowered)) {
                suggestions.add(candidate);
            }
        }

        suggestions.sort(String.CASE_INSENSITIVE_ORDER);

        if (suggestions.isEmpty()) {
            clearSuggestions();
            return;
        }

        visible = true;
        if (selection < 0 || selection >= suggestions.size()) {
            selection = 0;
        } else {
            selection = Math.min(selection, suggestions.size() - 1);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!visible) {
            return false;
        }

        if (keyCode == GLFW.GLFW_KEY_TAB) {
            return applySelection();
        }

        if (keyCode == GLFW.GLFW_KEY_UP) {
            moveSelection(-1);
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_DOWN) {
            moveSelection(1);
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            hide();
            return false;
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(double deltaY) {
        if (!visible) {
            return false;
        }
        if (deltaY > 0) {
            moveSelection(-1);
        } else if (deltaY < 0) {
            moveSelection(1);
        }
        return true;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible || suggestions.isEmpty() || currentToken == null) {
            return false;
        }
        if (button != 0) {
            return false;
        }

        int mx = (int) mouseX;
        int my = (int) mouseY;

        if (mx < boxLeft || mx > boxRight || my < boxTop || my > boxBottom) {
            return false;
        }

        int index = (my - boxTop - 2) / Math.max(1, lineHeight);
        if (index < 0 || index >= suggestions.size()) {
            return false;
        }

        selection = index;
        return applySelection();
    }

    private void moveSelection(int delta) {
        if (suggestions.isEmpty()) {
            selection = -1;
            return;
        }
        selection = (selection + delta + suggestions.size()) % suggestions.size();
    }

    private boolean applySelection() {
        if (!visible || suggestions.isEmpty() || currentToken == null) {
            return false;
        }

        String replacement = suggestions.get(selection);
        String value = input.getValue();

        int start = currentToken.startIndex() + 1; // skip '@'
        int end = currentToken.endIndex();

        String newValue = value.substring(0, start) + replacement + value.substring(end);
        input.setValue(newValue);

        int newCursor = start + replacement.length();
        input.setCursorPosition(newCursor);
        input.setHighlightPos(newCursor);

        clearSuggestions();
        return true;
    }

    private void clearSuggestions() {
        this.visible = false;
        this.selection = -1;
        this.currentToken = null;
    }

    public void markDirty() {
        this.dirty = true;
    }

    private boolean isFor(@NotNull ChatScreen other) {
        return this.screen == other;
    }

    private void rebuildContext() {
        mentionContextInstance.rebuild();
    }

    @Override
    public void hide() {
        clearSuggestions();
    }

    private void renderBox(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
        if (!visible || suggestions.isEmpty()) {
            return;
        }

        Font font = minecraft.font;
        this.lineHeight = font.lineHeight + 2;
        int maxWidth = suggestions.stream().mapToInt(font::width).max().orElse(0);

        int boxWidth = maxWidth + 8;
        int boxHeight = suggestions.size() * lineHeight + 4;

        int x = input.getX();
        int y = input.getY() - boxHeight - 2;

        if (y < 0) {
            y = input.getY() + input.getHeight() + 2;
        }

        int left = x;
        int right = x + boxWidth;
        int top = y;
        int bottom = y + boxHeight;

        this.boxLeft = left;
        this.boxRight = right;
        this.boxTop = top;
        this.boxBottom = bottom;

        graphics.fill(left - 2, top - 2, right, bottom, 0xC0101010);
        graphics.fill(left - 2, top - 2, right, top - 1, 0xC0000000);
        graphics.fill(left - 2, bottom, right, bottom + 1, 0xC0000000);

        for (int i = 0; i < suggestions.size(); i++) {
            String suggestion = suggestions.get(i);
            int textY = top + 2 + i * lineHeight;
            int color = (i == selection) ? 0xFFFFFF : 0xA0A0A0;
            graphics.drawString(font, suggestion, left, textY, color, false);
        }
    }

    public void renderWithUpdate(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
        if (!isFor((ChatScreen) Minecraft.getInstance().screen)) {
            return;
        }
        rebuildContext();
        tick();
        renderBox(graphics, mouseX, mouseY);
    }
}
