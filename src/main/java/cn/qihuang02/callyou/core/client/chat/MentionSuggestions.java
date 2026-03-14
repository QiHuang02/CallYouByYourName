package cn.qihuang02.callyou.core.client.chat;

import cn.qihuang02.callyou.api.client.ClientMentionMetadata;
import cn.qihuang02.callyou.core.mention.MentionTokens;
import cn.qihuang02.callyou.util.OnlinePlayerList;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom mention suggestion overlay for the chat screen.
 * Does NOT extend CommandSuggestions to avoid AT/access issues in Forge 1.20.1.
 */
public class MentionSuggestions {
    private final Minecraft minecraft;
    private final ChatScreen screen;
    private final EditBox input;
    private final Font font;

    @Nullable
    private MentionTokens.Token currentToken;
    @Nullable
    private Suggestions suggestions;
    private int selectedIndex = -1;
    private boolean visible = false;

    // Rendering constants
    private static final int SUGGESTION_HEIGHT = 12;
    private static final int MAX_VISIBLE = 10;
    private static final int BG_COLOR = 0xC0101010;
    private static final int HIGHLIGHT_COLOR = 0x80808080;
    private static final int TEXT_COLOR = 0xFFD0D0D0;
    private static final int HIGHLIGHT_TEXT_COLOR = 0xFFFFFF00;

    public MentionSuggestions(
            Minecraft minecraft,
            ChatScreen screen,
            EditBox input,
            Font font
    ) {
        this.minecraft = minecraft;
        this.screen = screen;
        this.input = input;
        this.font = font;
    }

    public boolean isSuggestionsVisible() {
        return this.visible && this.suggestions != null && !this.suggestions.isEmpty();
    }

    public void markDirty() {
        rebuildContext();
    }

    private void rebuildContext() {
        this.currentToken = null;
        this.suggestions = null;
        this.selectedIndex = -1;
        this.visible = false;

        String value = this.input.getValue();
        int caret = this.input.getCursorPosition();
        if (value.isEmpty() || caret == 0) {
            return;
        }

        for (MentionTokens.Token token : MentionTokens.scan(value)) {
            if (token.startIndex() <= caret
                    && caret >= token.startIndex() + 1
                    && caret <= token.endIndex()) {
                this.currentToken = token;
                break;
            }
        }

        if (this.currentToken == null) {
            int before = caret - 1;
            if (before >= 0 && value.charAt(before) == '@') {
                this.currentToken = new MentionTokens.Token(before, caret, "");
            } else {
                return;
            }
        }

        String prefix = value.substring(this.currentToken.startIndex() + 1, caret);
        List<String> candidates = collectCandidates(prefix);
        if (candidates.isEmpty()) {
            return;
        }

        buildSuggestions(value, candidates);
    }

    private @NotNull List<String> collectCandidates(@NotNull String prefix) {
        List<String> result = new ArrayList<>();
        ClientMentionMetadata mentionContext = ClientMentionMetadata.ClientMentionMetadataCache.get(this.minecraft);
        List<String> typeKeys = mentionContext.getMentionTypeKeysSorted();
        if (prefix.isEmpty()) {
            for (String typeKey : typeKeys) {
                if ("player".equals(typeKey)) continue;
                result.add(typeKey);
            }
            result.addAll(OnlinePlayerList.getClientOnlinePlayerNames(this.minecraft));
            return result;
        }

        for (String typeKey : typeKeys) {
            if ("player".equals(typeKey)) continue;
            if (typeKey.regionMatches(true, 0, prefix, 0, prefix.length())) {
                result.add(typeKey);
            }
        }

        for (String playerName : OnlinePlayerList.getClientOnlinePlayerNames(this.minecraft)) {
            if (playerName.regionMatches(true, 0, prefix, 0, prefix.length())) {
                result.add(playerName);
            }
        }
        return result;
    }

    private void buildSuggestions(String fullInput, List<String> candidates) {
        if (this.currentToken == null) {
            return;
        }

        int start = this.currentToken.startIndex() + 1; // skip '@'

        SuggestionsBuilder builder = new SuggestionsBuilder(fullInput, start);
        for (String c : candidates) {
            builder.suggest(c);
        }

        this.suggestions = builder.build();
        this.selectedIndex = 0;
        this.visible = true;
    }

    public void renderWithUpdate(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!isSuggestionsVisible() || this.suggestions == null) {
            return;
        }

        List<Suggestion> list = this.suggestions.getList();
        int count = Math.min(list.size(), MAX_VISIBLE);
        if (count == 0) return;

        int maxWidth = 0;
        for (Suggestion s : list) {
            int w = this.font.width(s.getText());
            if (w > maxWidth) maxWidth = w;
        }
        maxWidth += 2;

        int x = getXOffset();
        int totalHeight = count * SUGGESTION_HEIGHT;
        int y = this.screen.height - 14 - totalHeight - 2;

        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 400.0F);

        // Background
        graphics.fill(x - 1, y - 1, x + maxWidth + 1, y + totalHeight, BG_COLOR);

        for (int i = 0; i < count; i++) {
            Suggestion suggestion = list.get(i);
            boolean selected = (i == this.selectedIndex);
            int sy = y + i * SUGGESTION_HEIGHT;

            if (selected) {
                graphics.fill(x - 1, sy, x + maxWidth + 1, sy + SUGGESTION_HEIGHT, HIGHLIGHT_COLOR);
            }

            graphics.drawString(this.font, suggestion.getText(), x, sy + 2,
                    selected ? HIGHLIGHT_TEXT_COLOR : TEXT_COLOR);
        }

        graphics.pose().popPose();
    }

    private int getXOffset() {
        if (this.currentToken == null) return 2;
        String beforeToken = this.input.getValue().substring(0, this.currentToken.startIndex());
        return this.font.width(beforeToken) + 4;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!isSuggestionsVisible() || this.suggestions == null) {
            return false;
        }

        List<Suggestion> list = this.suggestions.getList();
        int count = Math.min(list.size(), MAX_VISIBLE);

        // Tab or Enter to accept
        if (keyCode == 258 || keyCode == 257) { // Tab or Enter
            if (this.selectedIndex >= 0 && this.selectedIndex < count) {
                applySuggestion(list.get(this.selectedIndex));
                return true;
            }
        }

        // Up arrow
        if (keyCode == 265) {
            this.selectedIndex = (this.selectedIndex - 1 + count) % count;
            return true;
        }

        // Down arrow
        if (keyCode == 264) {
            this.selectedIndex = (this.selectedIndex + 1) % count;
            return true;
        }

        // Escape
        if (keyCode == 256) {
            hide();
            return true;
        }

        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (!isSuggestionsVisible() || this.suggestions == null) {
            return false;
        }

        int count = Math.min(this.suggestions.getList().size(), MAX_VISIBLE);
        if (delta > 0) {
            this.selectedIndex = (this.selectedIndex - 1 + count) % count;
        } else if (delta < 0) {
            this.selectedIndex = (this.selectedIndex + 1) % count;
        }
        return true;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!isSuggestionsVisible() || this.suggestions == null) {
            return false;
        }

        List<Suggestion> list = this.suggestions.getList();
        int count = Math.min(list.size(), MAX_VISIBLE);

        int x = getXOffset();
        int totalHeight = count * SUGGESTION_HEIGHT;
        int y = this.screen.height - 14 - totalHeight - 2;

        if (mouseX >= x - 1 && mouseX <= x + 200 && mouseY >= y && mouseY < y + totalHeight) {
            int index = (int) ((mouseY - y) / SUGGESTION_HEIGHT);
            if (index >= 0 && index < count) {
                applySuggestion(list.get(index));
                return true;
            }
        }

        return false;
    }

    private void applySuggestion(Suggestion suggestion) {
        if (this.currentToken == null) return;

        String value = this.input.getValue();
        int start = this.currentToken.startIndex() + 1; // after '@'
        int caret = this.input.getCursorPosition();

        String before = value.substring(0, start);
        String after = caret < value.length() ? value.substring(caret) : "";
        String newValue = before + suggestion.getText() + " " + after;

        this.input.setValue(newValue);
        this.input.setCursorPosition(start + suggestion.getText().length() + 1);

        hide();
    }

    public void hide() {
        this.visible = false;
        this.suggestions = null;
        this.currentToken = null;
        this.selectedIndex = -1;
    }
}
