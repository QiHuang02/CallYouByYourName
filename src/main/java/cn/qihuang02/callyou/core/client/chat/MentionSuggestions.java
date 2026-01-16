package cn.qihuang02.callyou.core.client.chat;

import cn.qihuang02.callyou.api.client.ClientMentionContext;
import cn.qihuang02.callyou.core.MentionTokens;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MentionSuggestions extends CommandSuggestions {
    private final Minecraft minecraft;
    private final EditBox input;

    @Nullable
    private MentionTokens.Token currentToken;

    public MentionSuggestions(
            Minecraft minecraft,
            ChatScreen screen,
            EditBox input,
            Font font
    ) {
        super(
                minecraft,
                screen,
                input,
                font,
                false,
                false,
                0,
                10,
                true,
                0xC0101010
        );

        this.minecraft = minecraft;
        this.input = input;

        this.setAllowSuggestions(false);
        this.setAllowHiding(true);
    }

    public void markDirty() {
        rebuildContext();
    }

    private void rebuildContext() {
        this.currentToken = null;
        this.pendingSuggestions = null;
        this.suggestions = null;
        this.setAllowSuggestions(false);

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
        ClientMentionContext context = ClientMentionContext.get(this.minecraft);
        List<String> typeKeys = context.getMentionTypeKeysSorted();
        List<String> onlinePlayers = context.getOnlinePlayerNames();
        if (prefix.isEmpty()) {
            for (String typeKey : typeKeys) {
                if ("player".equals(typeKey)) continue;
                result.add(typeKey);
            }
            result.addAll(onlinePlayers);
            return result;
        }

        for (String typeKey : typeKeys) {
            if ("player".equals(typeKey)) continue;

            if (typeKey.regionMatches(true, 0, prefix, 0, prefix.length())) {
                result.add(typeKey);
            }
        }

        for (String playerName : onlinePlayers) {
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

        Suggestions built = builder.build();

        this.pendingSuggestions = CompletableFuture.completedFuture(built);
        this.setAllowSuggestions(true);
        this.showSuggestions(false);
    }

    public void renderWithUpdate(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!this.isVisible()) {
            return;
        }

        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 400.0F);

        super.render(graphics, mouseX, mouseY);

        graphics.pose().popPose();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.isVisible()) {
            return false;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double delta) {
        if (!this.isVisible()) {
            return false;
        }
        return super.mouseScrolled(delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.isVisible()) {
            return false;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void hide() {
        super.hide();
        this.currentToken = null;
    }
}
