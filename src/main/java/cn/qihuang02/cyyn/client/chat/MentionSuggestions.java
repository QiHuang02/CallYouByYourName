package cn.qihuang02.cyyn.client.chat;

import cn.qihuang02.cyyn.util.MentionCandidateProvider;
import cn.qihuang02.cyyn.util.MentionTextUtils;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
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
    private final MentionCandidateProvider candidateProvider;
    private int replaceStart;
    private int replaceEnd;

    public MentionSuggestions(@NotNull Minecraft minecraft, @NotNull Screen screen, @NotNull EditBox input) {
        super(minecraft, screen, input, minecraft.font, false, false, 0, MAX_VISIBLE, true, BACKGROUND_COLOR);
        this.minecraft = minecraft;
        this.input = input;
        this.candidateProvider = new MentionCandidateProvider(minecraft);
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

    private static boolean isCommandInput(@NotNull String value) {
        return !value.isEmpty() && value.charAt(0) == '/';
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

        Optional<MentionTextUtils.MentionTokenRange> rangeOptional = findMentionRangeAtCursor(value, cursor);
        if (rangeOptional.isEmpty()) {
            hide();
            return;
        }

        MentionTextUtils.MentionTokenRange range = rangeOptional.get();
        if (cursor <= range.mentionStart() || cursor > range.tokenEnd()) {
            hide();
            return;
        }

        this.replaceStart = range.tokenStart();
        this.replaceEnd = range.tokenEnd();

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

    private @NotNull Optional<MentionTextUtils.MentionTokenRange> findMentionRangeAtCursor(@NotNull String value, int cursor) {
        int searchIndex = 0;
        int boundedCursor = Math.max(0, Math.min(cursor, value.length()));
        while (searchIndex < value.length()) {
            Optional<MentionTextUtils.MentionTokenRange> optional = MentionTextUtils.findTokenRange(value, searchIndex);
            if (optional.isEmpty()) {
                return Optional.empty();
            }
            MentionTextUtils.MentionTokenRange range = optional.get();
            if (boundedCursor <= range.mentionStart()) {
                return Optional.empty();
            }
            if (boundedCursor <= range.tokenEnd()) {
                return optional;
            }
            searchIndex = range.tokenEnd();
        }
        return Optional.empty();
    }

    @Contract("_ -> new")
    private @NotNull Suggestions buildSuggestions(@NotNull String typed) {
        ClientPacketListener connection = this.minecraft.getConnection();

        LocalPlayer player = this.minecraft.player;

        StringRange range = StringRange.between(this.replaceStart, this.replaceEnd);
        String lowerTyped = typed.toLowerCase(Locale.ROOT);
        List<String> groupCandidates = this.candidateProvider.getGroupCandidates(player);
        List<String> playerCandidates = this.candidateProvider.getPlayerCandidates(connection, player);
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
