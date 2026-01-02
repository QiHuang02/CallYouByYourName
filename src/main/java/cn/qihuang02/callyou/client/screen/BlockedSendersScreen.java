package cn.qihuang02.callyou.client.screen;

import cn.qihuang02.callyou.core.attachment.MentionPreferences;
import com.lowdragmc.lowdraglib2.gui.holder.ModularUIScreen;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextField;
import com.lowdragmc.lowdraglib2.gui.ui.style.LayoutStyle;
import com.lowdragmc.lowdraglib2.gui.ui.style.Stylesheet;
import com.lowdragmc.lowdraglib2.gui.ui.style.StylesheetManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.appliedenergistics.yoga.YogaAlign;
import org.appliedenergistics.yoga.YogaFlexDirection;
import org.appliedenergistics.yoga.YogaJustify;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BlockedSendersScreen extends ModularUIScreen {
    private static final int PANEL_WIDTH = 360;
    private static final int PANEL_HEIGHT = 260;
    private static final Component TITLE = Component.translatable("screen.callyou.blocked_senders.title");
    private static final Component UNBLOCK = Component.translatable("screen.callyou.blocked_senders.unblock");
    private static final Component EMPTY = Component.translatable("screen.callyou.mention_preferences.blocked.none");
    private static final Component BLOCK_LABEL = Component.translatable("screen.callyou.blocked_senders.block_label");
    private static final Component BLOCK_ACTION = Component.translatable("screen.callyou.blocked_senders.block_action");
    private static final Component BLOCK_ACTION_TOOLTIP = Component.translatable("screen.callyou.blocked_senders.block_action.tooltip");
    private static final Component BLOCK_INPUT_TOOLTIP = Component.translatable("screen.callyou.blocked_senders.block_input.tooltip");
    private static final Component ERROR_NOT_FOUND = Component.translatable("screen.callyou.blocked_senders.error.not_found");
    private static final Component ERROR_ALREADY_BLOCKED = Component.translatable("screen.callyou.blocked_senders.error.already_blocked");

    private final MentionPreferencesScreen parent;
    private final MentionPreferences preferences;
    private final boolean controlsActive;
    private final TextField blockInput;
    private final Button blockButton;
    private final Label blockStatusLabel;
    private final ScrollerView senderList;
    private final Label emptyLabel;
    private final Button doneButton;
    private Component blockStatus = Component.empty();
    private int blockStatusColor = 0xAAAAAA;

    protected BlockedSendersScreen(@NotNull MentionPreferencesScreen parent, @NotNull MentionPreferences preferences,
                                   boolean controlsActive) {
        this(parent, preferences, controlsActive, buildUIRefs());
    }

    private BlockedSendersScreen(@NotNull MentionPreferencesScreen parent, @NotNull MentionPreferences preferences,
                                 boolean controlsActive, @NotNull UIRefs refs) {
        super(refs.modularUI, TITLE);
        this.parent = Objects.requireNonNull(parent);
        this.preferences = Objects.requireNonNull(preferences);
        this.controlsActive = controlsActive;
        this.blockInput = refs.blockInput;
        this.blockButton = refs.blockButton;
        this.blockStatusLabel = refs.blockStatusLabel;
        this.senderList = refs.senderList;
        this.emptyLabel = refs.emptyLabel;
        this.doneButton = refs.doneButton;
        this.configureActions();
    }

    private void configureActions() {
        this.blockInput.setTextValidator(text -> text.length() <= 16);
        this.blockInput.setTextResponder(value -> this.updateBlockControls());
        this.blockInput.style(style -> style.tooltips(BLOCK_INPUT_TOOLTIP));

        this.blockButton.style(style -> style.tooltips(BLOCK_ACTION_TOOLTIP));
        this.blockButton.setOnClick(event -> this.attemptBlockSender());

        this.doneButton.setOnClick(event -> onClose());
    }

    @Override
    public void init() {
        super.init();
        this.reloadBlockedSenders();
        this.updateBlockControls();
        this.setStatusMessage(this.blockStatus, this.blockStatusColor);
    }

    @Override
    public void tick() {
        super.tick();
        this.getModularUI().tick();
    }

    @Override
    public void removed() {
        super.removed();
        this.getModularUI().onRemoved();
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    private void attemptBlockSender() {
        String input = this.blockInput.getText().trim();
        if (input.isEmpty()) {
            return;
        }

        UUID senderId = this.resolveOnlinePlayer(input);
        if (senderId == null) {
            this.showBlockError(ERROR_NOT_FOUND);
            return;
        }

        if (this.preferences.getBlockedSenders().contains(senderId)) {
            this.showBlockError(ERROR_ALREADY_BLOCKED);
            return;
        }

        this.preferences.blockSender(senderId);
        this.parent.onBlockedSendersChanged();
        this.reloadBlockedSenders();
        this.blockInput.setText("", false);
        this.setStatusMessage(Component.empty(), 0xAAAAAA);
        this.updateBlockControls();
    }

    private @Nullable UUID resolveOnlinePlayer(@NotNull String name) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return null;
        }
        ClientPacketListener connection = minecraft.getConnection();
        if (connection == null) {
            return null;
        }
        for (PlayerInfo info : connection.getOnlinePlayers()) {
            String profileName = info.getProfile().getName();
            if (profileName != null && profileName.equalsIgnoreCase(name)) {
                return info.getProfile().getId();
            }
        }
        return null;
    }

    private void showBlockError(@NotNull Component message) {
        this.setStatusMessage(message, 0xFF5555);
    }

    private void setStatusMessage(@NotNull Component message, int color) {
        this.blockStatus = message;
        this.blockStatusColor = color;
        this.blockStatusLabel.setText(message);
        this.blockStatusLabel.textStyle(style -> style.textColor(color));
    }

    private void updateBlockControls() {
        String value = this.blockInput.getText();
        boolean hasInput = !value.trim().isEmpty();
        this.blockButton.setActive(this.controlsActive && hasInput);
        this.blockInput.setActive(this.controlsActive);
        this.blockInput.setFocusable(this.controlsActive);
    }

    private @NotNull Component resolveDisplayName(@NotNull UUID id) {
        if (this.minecraft != null) {
            ClientPacketListener connection = this.minecraft.getConnection();
            if (connection != null) {
                PlayerInfo info = connection.getPlayerInfo(id);
                if (info != null) {
                    Component displayName = info.getTabListDisplayName();
                    if (displayName != null) {
                        return displayName;
                    }
                    String profileName = info.getProfile().getName();
                    if (profileName != null && !profileName.isEmpty()) {
                        return Component.literal(profileName);
                    }
                }
            }
        }
        return Component.literal(id.toString());
    }

    private void reloadBlockedSenders() {
        List<UUID> sorted = new ArrayList<>(this.preferences.getBlockedSenders());
        sorted.sort(Comparator.comparing(uuid -> this.resolveDisplayName(uuid).getString(), String.CASE_INSENSITIVE_ORDER));

        this.senderList.clearAllScrollViewChildren();
        if (sorted.isEmpty()) {
            this.emptyLabel.setVisible(true);
            this.senderList.addScrollViewChild(this.emptyLabel);
            return;
        }

        this.emptyLabel.setVisible(false);
        for (UUID id : sorted) {
            this.senderList.addScrollViewChild(this.buildSenderRow(id));
        }
    }

    private @NotNull UIElement buildSenderRow(@NotNull UUID senderId) {
        Label nameLabel = new Label();
        nameLabel.setText(this.resolveDisplayName(senderId));
        nameLabel.layout(LayoutStyle::widthStretch);

        Label uuidLabel = new Label();
        uuidLabel.setText(Component.literal(senderId.toString()));
        uuidLabel.textStyle(style -> style.textColor(0x888888));
        uuidLabel.layout(LayoutStyle::widthStretch);

        UIElement info = new UIElement()
                .layout(style -> style.flexDirection(YogaFlexDirection.COLUMN)
                        .gapRow(1)
                        .flexGrow(1))
                .addChildren(nameLabel, uuidLabel);

        Button unblockButton = new Button();
        unblockButton.setText(UNBLOCK);
        unblockButton.layout(style -> style.width(70).height(16));
        unblockButton.setOnClick(event -> {
            this.preferences.unblockSender(senderId);
            this.parent.onBlockedSendersChanged();
            this.reloadBlockedSenders();
        });
        unblockButton.setActive(this.controlsActive);

        return new UIElement()
                .layout(style -> style.flexDirection(YogaFlexDirection.ROW)
                        .alignItems(YogaAlign.CENTER)
                        .gapColumn(8)
                        .widthStretch()
                        .height(28))
                .addChildren(info, unblockButton);
    }

    @Contract(" -> new")
    private static @NotNull UIRefs buildUIRefs() {
        Label titleLabel = new Label();
        titleLabel.setText(TITLE);
        titleLabel.textStyle(style -> style.textAlignHorizontal(Horizontal.CENTER));
        titleLabel.layout(LayoutStyle::widthStretch);

        Label inputLabel = new Label();
        inputLabel.setText(BLOCK_LABEL);
        inputLabel.layout(LayoutStyle::widthStretch);

        TextField blockInput = new TextField();
        blockInput.layout(style -> style.flexGrow(1).height(16));

        Button blockButton = new Button();
        blockButton.setText(BLOCK_ACTION);
        blockButton.layout(style -> style.width(70).height(16));

        UIElement inputRow = new UIElement()
                .layout(style -> style.flexDirection(YogaFlexDirection.ROW)
                        .alignItems(YogaAlign.CENTER)
                        .gapColumn(8)
                        .widthStretch())
                .addChildren(blockInput, blockButton);

        Label statusLabel = new Label();
        statusLabel.setText(Component.empty());
        statusLabel.textStyle(style -> style.textColor(0xAAAAAA));
        statusLabel.layout(LayoutStyle::widthStretch);

        ScrollerView senderList = new ScrollerView();
        senderList.layout(style -> style.flexGrow(1).widthStretch());
        senderList.viewContainer(container -> container.layout(layout -> layout.flexDirection(YogaFlexDirection.COLUMN)
                .gapRow(2)
                .widthStretch()));

        Label emptyLabel = new Label();
        emptyLabel.setText(EMPTY);
        emptyLabel.textStyle(style -> style.textAlignHorizontal(Horizontal.CENTER).textColor(0xAAAAAA));
        emptyLabel.layout(style -> style.widthStretch().height(20));

        Button doneButton = new Button();
        doneButton.setText(CommonComponents.GUI_DONE);
        doneButton.layout(style -> style.width(100).height(16));

        UIElement buttonRow = new UIElement()
                .layout(style -> style.flexDirection(YogaFlexDirection.ROW)
                        .alignItems(YogaAlign.CENTER)
                        .justifyItems(YogaJustify.FLEX_END)
                        .widthStretch())
                .addChildren(doneButton);

        UIElement root = new UIElement()
                .addClass("panel_bg")
                .layout(style -> style.width(PANEL_WIDTH)
                        .height(PANEL_HEIGHT)
                        .flexDirection(YogaFlexDirection.COLUMN)
                        .alignItems(YogaAlign.STRETCH))
                .addChildren(titleLabel, inputLabel, inputRow, statusLabel, senderList, buttonRow);

        Stylesheet mcStyle = StylesheetManager.INSTANCE.getStylesheetSafe(StylesheetManager.MC);
        ModularUI modularUI = ModularUI.of(UI.of(root, mcStyle));

        return new UIRefs(modularUI, blockInput, blockButton, statusLabel, senderList, emptyLabel, doneButton);
    }

    private record UIRefs(
            ModularUI modularUI,
            TextField blockInput,
            Button blockButton,
            Label blockStatusLabel,
            ScrollerView senderList,
            Label emptyLabel,
            Button doneButton
    ) {
    }
}
