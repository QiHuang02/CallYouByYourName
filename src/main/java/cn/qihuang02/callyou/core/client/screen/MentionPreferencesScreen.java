package cn.qihuang02.callyou.core.client.screen;

import cn.qihuang02.callyou.core.attachment.MentionPreferences;
import cn.qihuang02.callyou.core.client.ClientMentionHistory;
import cn.qihuang02.callyou.core.client.ClientMentionPreferences;
import cn.qihuang02.callyou.core.client.screen.row.BlockedSenderRow;
import cn.qihuang02.callyou.core.client.screen.row.MentionHistoryRow;
import cn.qihuang02.callyou.core.client.screen.row.MentionTypeRow;
import cn.qihuang02.callyou.core.network.NetworkHandler;
import cn.qihuang02.callyou.core.network.payload.c2s.MentionLogActionPayload;
import cn.qihuang02.callyou.core.saveddata.MentionRecord;
import cn.qihuang02.callyou.registry.CallYouMentionRegistries;
import com.lowdragmc.lowdraglib2.gui.holder.ModularUIScreen;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.*;
import com.lowdragmc.lowdraglib2.gui.ui.styletemplate.Sprites;
import com.lowdragmc.lowdraglib2.utils.XmlUtils;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.appliedenergistics.yoga.YogaAlign;
import org.appliedenergistics.yoga.YogaFlexDirection;
import org.appliedenergistics.yoga.YogaJustify;
import org.appliedenergistics.yoga.YogaPositionType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MentionPreferencesScreen extends ModularUIScreen {
    private static final int PANEL_WIDTH = 280;
    private static final int PANEL_HEIGHT = 220;
    private static final int TOAST_HEIGHT = 20;
    private static final int TOAST_PADDING = 6;
    private static final long TOAST_DURATION_MS = 2000L;

    private static final ResourceLocation UI_XML = ResourceLocation.parse("callyou:ui/mention_preferences.xml");

    private static final Component TITLE = Component.translatable("screen.callyou.mention_preferences.title");

    private static final Component BLOCK_ACTION_TOOLTIP = Component.translatable("screen.callyou.blocked_senders.block_action.tooltip");
    private static final Component ERROR_NOT_FOUND = Component.translatable("screen.callyou.blocked_senders.error.not_found");
    private static final Component ERROR_ALREADY_BLOCKED = Component.translatable("screen.callyou.blocked_senders.error.already_blocked");
    private static final Component HISTORY_EMPTY = Component.translatable("screen.callyou.history.empty");
    private static final Component HISTORY_LOADING = Component.translatable("screen.callyou.history.loading");

    private final Screen parent;
    // --- UI References ---
    private final Button tabGeneralButton;
    private final Button tabBlockedButton;
    private final Button tabHistoryButton;
    // Tab Containers
    private final UIElement generalTabContainer;
    private final UIElement blockedTabContainer;
    private final UIElement historyTabContainer;
    // General Tab Components
    private final Switch allowMentionsSwitch;
    private final Switch allowMassMentionsSwitch;
    private final TextField typeSearchInput;
    private final ScrollerView mentionTypeList;
    private final List<MentionTypeRow> mentionTypeRows = new ArrayList<>();
    // Blocked Tab Components
    private final TextField playerSearchInput;
    private final Button blockButton;
    private final Label blockStatusLabel;
    private final ScrollerView blockedSenderList;
    private final Label blockedEmptyLabel;
    // History Tab Components
    private final Button historyRefreshButton;
    private final Button markAllReadButton;
    private final Label historyStatusLabel;
    private final ScrollerView historyList;
    private final Label historyEmptyLabel;
    private final Button doneButton;
    private final Label toastLabel;
    private final UIElement toastLayer;
    private MentionPreferences workingCopy;
    private List<MentionRecord> cachedHistory = new ArrayList<>();
    private Tab currentTab = Tab.GENERAL;
    private long lastUiRefreshTime = -1;
    private long lastHistoryRefreshTime = -1;
    private long toastHideAt = -1;

    public MentionPreferencesScreen(@Nullable Screen parent) {
        this(parent, new UIBuilder());
    }

    private MentionPreferencesScreen(@Nullable Screen parent, @NotNull UIBuilder builder) {
        super(builder.modularUI, TITLE);
        this.parent = parent;

        this.tabGeneralButton = builder.tabGeneralButton;
        this.tabBlockedButton = builder.tabBlockedButton;
        this.tabHistoryButton = builder.tabHistoryButton;
        this.generalTabContainer = builder.generalTabContainer;
        this.blockedTabContainer = builder.blockedTabContainer;
        this.historyTabContainer = builder.historyTabContainer;

        this.allowMentionsSwitch = builder.allowMentionsSwitch;
        this.allowMassMentionsSwitch = builder.allowMassMentionsSwitch;
        this.typeSearchInput = builder.typeSearchInput;
        this.mentionTypeList = builder.mentionTypeList;

        this.playerSearchInput = builder.playerSearchInput;
        this.blockButton = builder.blockButton;
        this.blockStatusLabel = builder.blockStatusLabel;
        this.blockedSenderList = builder.blockedSenderList;
        this.blockedEmptyLabel = builder.blockedEmptyLabel;

        this.historyRefreshButton = builder.historyRefreshButton;
        this.markAllReadButton = builder.markAllReadButton;
        this.historyStatusLabel = builder.historyStatusLabel;
        this.historyList = builder.historyList;
        this.historyEmptyLabel = builder.historyEmptyLabel;

        this.doneButton = builder.doneButton;
        this.toastLabel = builder.toastLabel;
        this.toastLayer = builder.toastLayer;

        this.configureActions();
    }

    @Contract("_ -> new")
    public static @NotNull MentionPreferencesScreen createWithRefresh(@Nullable Screen parent) {
        ClientMentionPreferences.markAwaitingSync();
        NetworkHandler.requestPreferencesSync();
        return new MentionPreferencesScreen(parent);
    }

    private void configureActions() {
        // Tab Switching
        this.tabGeneralButton.setOnClick(e -> switchTab(Tab.GENERAL));
        this.tabBlockedButton.setOnClick(e -> switchTab(Tab.BLOCKED));
        this.tabHistoryButton.setOnClick(e -> switchTab(Tab.HISTORY));

        // General Tab Actions
        this.allowMentionsSwitch.style(style -> style.tooltips(
                Component.translatable("screen.callyou.mention_preferences.allow_all.tooltip")));
        this.allowMentionsSwitch.setOnSwitchChanged(value -> {
            if (this.workingCopy != null) {
                this.workingCopy.setAllowMentions(value);
                this.refreshTypeButtons();
                this.sendUpdate();
            }
        });

        this.allowMassMentionsSwitch.style(style -> style.tooltips(
                Component.translatable("screen.callyou.mention_preferences.allow_mass.tooltip")));
        this.allowMassMentionsSwitch.setOnSwitchChanged(value -> {
            if (this.workingCopy != null) {
                this.workingCopy.setAllowMassMentions(value);
                this.refreshTypeButtons();
                this.sendUpdate();
            }
        });

        this.typeSearchInput.setTextResponder(s -> this.populateMentionTypeList());

        // Blocked Tab Actions
        this.playerSearchInput.setTextValidator(text -> text.length() <= 36);
        this.playerSearchInput.setTextResponder(value -> this.updateBlockControls());

        this.blockButton.style(style -> style.tooltips(BLOCK_ACTION_TOOLTIP));
        this.blockButton.setOnClick(event -> this.attemptBlockSender());

        this.doneButton.setOnClick(event -> onClose());

        this.historyRefreshButton.setOnClick(event -> requestHistory());
        this.markAllReadButton.setOnClick(event ->
                NetworkHandler.sendMentionLogAction(
                        MentionLogActionPayload.MentionLogAction.MARK_ALL_READ,
                        Util.NIL_UUID
                ));
    }

    private void switchTab(Tab tab) {
        this.currentTab = tab;
        this.generalTabContainer.setVisible(tab == Tab.GENERAL);
        this.generalTabContainer.setDisplay(tab == Tab.GENERAL);
        this.blockedTabContainer.setVisible(tab == Tab.BLOCKED);
        this.blockedTabContainer.setDisplay(tab == Tab.BLOCKED);
        this.historyTabContainer.setVisible(tab == Tab.HISTORY);
        this.historyTabContainer.setDisplay(tab == Tab.HISTORY);

        this.tabGeneralButton.setActive(tab != Tab.GENERAL);
        this.tabBlockedButton.setActive(tab != Tab.BLOCKED);
        this.tabHistoryButton.setActive(tab != Tab.HISTORY);

        if (tab == Tab.GENERAL) {
            this.populateMentionTypeList();
        } else if (tab == Tab.BLOCKED) {
            this.reloadBlockedSenders();
            this.updateBlockControls();
        } else {
            this.requestHistory();
            this.updateHistoryControls();
        }
    }

    @Override
    public void init() {
        super.init();
        this.requestLatestPreferences();
        this.workingCopy = ClientMentionPreferences.copy();

        this.allowMentionsSwitch.setOn(this.workingCopy.isAllowMentions(), false);
        this.allowMassMentionsSwitch.setOn(this.workingCopy.isAllowMassMentions(), false);

        this.cachedHistory.clear();
        this.historyStatusLabel.setText(HISTORY_EMPTY);
        this.historyEmptyLabel.setVisible(false);

        this.switchTab(this.currentTab);
    }

    @Override
    public void tick() {
        super.tick();
        this.getModularUI().tick();
        this.refreshHistoryIfNeeded();
        this.updateToast();
        if (ClientMentionPreferences.isSyncPending()) {
            return;
        }

        long latestSync = ClientMentionPreferences.getLastSyncMillis();
        if (this.lastUiRefreshTime == latestSync) {
            return;
        }
        this.lastUiRefreshTime = latestSync;

        this.workingCopy = ClientMentionPreferences.copy();

        this.refreshTypeButtons();
        this.allowMentionsSwitch.setOn(this.workingCopy.isAllowMentions(), false);
        this.allowMassMentionsSwitch.setOn(this.workingCopy.isAllowMassMentions(), false);

        if (this.currentTab == Tab.BLOCKED) {
            this.reloadBlockedSenders();
        } else if (this.currentTab == Tab.GENERAL) {
            this.populateMentionTypeList();
        }
        this.updateBlockControls();
    }

    public void showTransientMessage(@NotNull Component message) {
        toastLabel.setText(message);
        layoutToastForMessage(message);
        toastLabel.setVisible(true);
        toastLabel.setDisplay(true);
        toastLayer.setVisible(true);
        toastLayer.setDisplay(true);
        toastHideAt = Util.getMillis() + TOAST_DURATION_MS;
    }

    private void updateToast() {
        if (toastHideAt <= 0) {
            return;
        }
        if (Util.getMillis() < toastHideAt) {
            return;
        }
        toastHideAt = -1;
        toastLabel.setVisible(false);
        toastLabel.setDisplay(false);
        toastLayer.setVisible(false);
        toastLayer.setDisplay(false);
    }

    private void layoutToastForMessage(@NotNull Component message) {
        Minecraft minecraft = Minecraft.getInstance();
        int textWidth = minecraft.font.width(message);
        int maxWidth = PANEL_WIDTH - TOAST_PADDING * 2;
        int toastWidth = Math.min(maxWidth, textWidth + TOAST_PADDING * 2);
        int left = Math.max(0, (PANEL_WIDTH - toastWidth) / 2);
        toastLayer.layout(style -> style.positionType(YogaPositionType.ABSOLUTE)
                .left(left)
                .top((PANEL_HEIGHT - TOAST_HEIGHT) / 2)
                .width(toastWidth)
                .height(TOAST_HEIGHT)
                .alignItems(YogaAlign.STRETCH)
                .justifyItems(YogaJustify.CENTER)
                .paddingLeft(TOAST_PADDING)
                .paddingRight(TOAST_PADDING));
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    private void populateMentionTypeList() {
        if (this.minecraft == null || this.minecraft.level == null) {
            return;
        }
        List<ResourceLocation> mentionTypes = this.resolveMentionTypes();
        String query = this.typeSearchInput.getText().trim().toLowerCase(Locale.ROOT);

        this.mentionTypeRows.clear();
        this.mentionTypeList.clearAllScrollViewChildren();

        for (ResourceLocation id : mentionTypes) {
            if (!query.isEmpty()) {
                String display = this.buildComponent(id).getString().toLowerCase(Locale.ROOT);
                String idText = id.toString().toLowerCase(Locale.ROOT);
                if (!display.contains(query) && !idText.contains(query)) {
                    continue;
                }
            }

            MentionTypeRow row = new MentionTypeRow(
                    id,
                    this.buildComponent(id),
                    this.workingCopy,
                    this::sendUpdate,
                    this::refreshTypeButtons
            );
            this.mentionTypeRows.add(row);
            this.mentionTypeList.addScrollViewChild(row.getElement());
        }
        this.refreshTypeButtons();
    }

    private void refreshTypeButtons() {
        if (this.workingCopy == null || this.minecraft == null || this.minecraft.level == null) {
            return;
        }

        boolean allowMentions = this.workingCopy.isAllowMentions();
        boolean allowMass = this.workingCopy.isAllowMassMentions();

        for (MentionTypeRow row : this.mentionTypeRows) {
            row.updateState(allowMentions, allowMass, this.workingCopy);
        }

        // Only show ON if global allowMentions is also ON
        this.allowMassMentionsSwitch.setOn(allowMentions && allowMass, false);
        this.allowMassMentionsSwitch.setActive(allowMentions);
    }

    private @NotNull Component buildComponent(@NotNull ResourceLocation id) {
        String key = "mention_type." + id.getNamespace() + "." + id.getPath();
        if (I18n.exists(key)) {
            return Component.translatable(key);
        }
        return Component.literal(id.toString());
    }

    private List<ResourceLocation> resolveMentionTypes() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return List.of();
        }
        Optional<List<ResourceLocation>> result = mc.level.registryAccess()
                .registry(CallYouMentionRegistries.MENTION_TYPE_REGISTRY_KEY)
                .map(registry -> registry.entrySet().stream()
                        .map(entry -> entry.getKey().location())
                        .sorted(Comparator.comparing(ResourceLocation::toString))
                        .toList());
        return result.orElseGet(List::of);
    }

    private void attemptBlockSender() {
        if (this.workingCopy == null) {
            return;
        }
        String input = this.playerSearchInput.getText().trim();
        if (input.isEmpty()) {
            return;
        }

        UUID senderId = this.resolveOnlinePlayer(input);
        if (senderId == null) {
            try {
                senderId = UUID.fromString(input);
            } catch (IllegalArgumentException e) {
                // Ignore
            }
        }

        if (senderId == null) {
            this.showBlockError(ERROR_NOT_FOUND);
            return;
        }

        if (this.workingCopy.getBlockedSenders().contains(senderId)) {
            this.showBlockError(ERROR_ALREADY_BLOCKED);
            return;
        }

        this.workingCopy.blockSender(senderId);
        this.onBlockedSendersChanged();
        this.playerSearchInput.setText("", false);
        this.setStatusMessage(Component.empty(), 0xAAAAAA);
        this.updateBlockControls();
    }

    private void reloadBlockedSenders() {
        this.blockedSenderList.clearAllScrollViewChildren();
        if (this.workingCopy == null) {
            this.blockedEmptyLabel.setVisible(true);
            this.blockedSenderList.addScrollViewChild(this.blockedEmptyLabel);
            return;
        }

        List<UUID> sorted = new ArrayList<>(this.workingCopy.getBlockedSenders());
        sorted.sort(Comparator.comparing(uuid -> this.resolveDisplayName(uuid).getString(), String.CASE_INSENSITIVE_ORDER));

        if (sorted.isEmpty()) {
            this.blockedEmptyLabel.setVisible(true);
            this.blockedSenderList.addScrollViewChild(this.blockedEmptyLabel);
            return;
        }

        this.blockedEmptyLabel.setVisible(false);
        for (UUID id : sorted) {
            this.blockedSenderList.addScrollViewChild(BlockedSenderRow.create(
                    id,
                    this.resolveDisplayName(id),
                    () -> {
                        if (this.workingCopy != null) {
                            this.workingCopy.unblockSender(id);
                            this.onBlockedSendersChanged();
                        }
                    }
            ));
        }
    }

    private void onBlockedSendersChanged() {
        this.sendUpdate();
        if (this.currentTab == Tab.BLOCKED) {
            this.reloadBlockedSenders();
            this.updateBlockControls();
        }
    }

    private void updateBlockControls() {
        boolean controlsActive = this.workingCopy != null && this.currentTab == Tab.BLOCKED;
        String value = this.playerSearchInput.getText();
        boolean hasInput = !value.trim().isEmpty();
        this.blockButton.setActive(controlsActive && hasInput);
    }

    private void showBlockError(@NotNull Component message) {
        this.setStatusMessage(message, 0xFF5555);
    }

    private void setStatusMessage(@NotNull Component message, int color) {
        this.blockStatusLabel.setText(message);
        this.blockStatusLabel.textStyle(style -> style.textColor(color));
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

    private void requestHistory() {
        this.cachedHistory.clear();
        this.historyEmptyLabel.setVisible(false);
        this.historyList.clearAllScrollViewChildren();
        this.historyStatusLabel.setText(HISTORY_LOADING);
        NetworkHandler.requestMentionLogs();
    }

    private void refreshHistoryIfNeeded() {
        if (this.currentTab != Tab.HISTORY) {
            return;
        }
        if (ClientMentionHistory.isAwaitingResponse()) {
            this.historyStatusLabel.setText(HISTORY_LOADING);
            this.updateHistoryControls();
            return;
        }
        long latestHistory = ClientMentionHistory.lastUpdateMillis();
        if (this.lastHistoryRefreshTime != latestHistory) {
            this.lastHistoryRefreshTime = latestHistory;
            this.reloadHistoryList();
        }
        this.updateHistoryControls();
    }

    private void reloadHistoryList() {
        this.historyList.clearAllScrollViewChildren();
        List<MentionRecord> records = ClientMentionHistory.copy();
        records.sort(Comparator.comparingLong(MentionRecord::timestamp).reversed());
        this.cachedHistory = new ArrayList<>(records);

        if (records.isEmpty()) {
            this.historyEmptyLabel.setVisible(true);
            this.historyList.addScrollViewChild(this.historyEmptyLabel);
            this.historyStatusLabel.setText(HISTORY_EMPTY);
            return;
        }

        this.historyEmptyLabel.setVisible(false);
        for (MentionRecord record : records) {
            MentionHistoryRow row = new MentionHistoryRow(
                    record,
                    () -> this.deleteRecord(record),
                    () -> this.markRecordRead(record)
            );
            this.historyList.addScrollViewChild(row.getElement());
        }
        this.historyStatusLabel.setText(Component.translatable("screen.callyou.history.count", records.size()));
    }

    private void updateHistoryControls() {
        boolean loading = ClientMentionHistory.isAwaitingResponse();
        boolean hasData = !this.cachedHistory.isEmpty();
        this.historyRefreshButton.setActive(!loading);
        this.markAllReadButton.setActive(!loading && hasData);

        if (!loading && !hasData) {
            this.historyStatusLabel.setText(HISTORY_EMPTY);
        }
    }

    private void deleteRecord(@NotNull MentionRecord record) {
        NetworkHandler.sendMentionLogAction(
                MentionLogActionPayload.MentionLogAction.DELETE_SINGLE,
                record.historyId()
        );
    }

    private void markRecordRead(@NotNull MentionRecord record) {
        NetworkHandler.sendMentionLogAction(
                MentionLogActionPayload.MentionLogAction.MARK_SINGLE_READ,
                record.historyId()
        );
    }

    private void sendUpdate() {
        if (this.workingCopy == null) {
            return;
        }
        MentionPreferences payload = new MentionPreferences();
        payload.copyFrom(this.workingCopy);
        ClientMentionPreferences.markPending(payload);
        NetworkHandler.sendPreferenceUpdate(payload);
    }

    private void requestLatestPreferences() {
        if (!ClientMentionPreferences.isAwaitingSync()) {
            ClientMentionPreferences.markAwaitingSync();
            NetworkHandler.requestPreferencesSync();
        }
    }

    private enum Tab {GENERAL, BLOCKED, HISTORY}

    private static class UIBuilder {
        ModularUI modularUI;
        Button tabGeneralButton;
        Button tabBlockedButton;
        Button tabHistoryButton;
        UIElement generalTabContainer;
        UIElement blockedTabContainer;
        UIElement historyTabContainer;

        // General
        Switch allowMentionsSwitch;
        Switch allowMassMentionsSwitch;
        TextField typeSearchInput;
        ScrollerView mentionTypeList;

        // Blocked
        TextField playerSearchInput;
        Button blockButton;
        Label blockStatusLabel;
        ScrollerView blockedSenderList;
        Label blockedEmptyLabel;

        // History
        Button historyRefreshButton;
        Button markAllReadButton;
        Label historyStatusLabel;
        ScrollerView historyList;
        Label historyEmptyLabel;

        // Shared
        Button doneButton;
        Label toastLabel;
        UIElement toastLayer;

        UIBuilder() {
            build();
        }

        private void build() {
            var xml = XmlUtils.loadXml(UI_XML);
            if (xml == null) {
                throw new IllegalStateException("UI xml not found: " + UI_XML);
            }

            UI ui = UI.of(xml);
            UIElement root = ui.getRootElement();
            root.style(style -> style.background(Sprites.RECT_SOLID));

            tabGeneralButton = require(ui, "#tab-general", Button.class);
            tabBlockedButton = require(ui, "#tab-blocked", Button.class);
            tabHistoryButton = require(ui, "#tab-history", Button.class);
            generalTabContainer = require(ui, "#tab-general-content", UIElement.class);
            blockedTabContainer = require(ui, "#tab-blocked-content", UIElement.class);
            historyTabContainer = require(ui, "#tab-history-content", UIElement.class);

            allowMentionsSwitch = require(ui, "#allow-mentions-switch", Switch.class);
            allowMassMentionsSwitch = require(ui, "#allow-mass-switch", Switch.class);
            typeSearchInput = require(ui, "#type-search-input", TextField.class);
            mentionTypeList = require(ui, "#mention-type-list", ScrollerView.class);

            playerSearchInput = require(ui, "#player-search-input", TextField.class);
            blockButton = require(ui, "#block-button", Button.class);
            blockStatusLabel = require(ui, "#block-status", Label.class);
            blockedSenderList = require(ui, "#blocked-sender-list", ScrollerView.class);
            blockedEmptyLabel = require(ui, "#blocked-empty-label", Label.class);

            historyRefreshButton = require(ui, "#history-refresh-button", Button.class);
            markAllReadButton = require(ui, "#history-mark-all", Button.class);
            historyStatusLabel = require(ui, "#history-status", Label.class);
            historyList = require(ui, "#history-list", ScrollerView.class);
            historyEmptyLabel = require(ui, "#history-empty-label", Label.class);

            doneButton = require(ui, "#done-button", Button.class);
            toastLayer = require(ui, "#toast-layer", UIElement.class);
            toastLabel = require(ui, "#toast-label", Label.class);

            MentionUIStyles.applyButtonStyle(tabGeneralButton);
            MentionUIStyles.applyButtonStyle(tabBlockedButton);
            MentionUIStyles.applyButtonStyle(tabHistoryButton);
            MentionUIStyles.applyButtonStyle(blockButton);
            MentionUIStyles.applyButtonStyle(historyRefreshButton);
            MentionUIStyles.applyButtonStyle(markAllReadButton);
            MentionUIStyles.applyButtonStyle(doneButton);
            tabGeneralButton.setText(Component.empty());
            tabBlockedButton.setText(Component.empty());
            tabHistoryButton.setText(Component.empty());
            tabGeneralButton.style(style -> style.tooltips(
                    Component.translatable("screen.callyou.mention_preferences.title")));
            tabBlockedButton.style(style -> style.tooltips(
                    Component.translatable("screen.callyou.blocked_senders.title")));
            tabHistoryButton.style(style -> style.tooltips(
                    Component.translatable("screen.callyou.history.title")));
            historyRefreshButton.setText(Component.empty());
            markAllReadButton.setText(Component.empty());
            historyRefreshButton.style(style -> style.tooltips(
                    Component.translatable("screen.callyou.history.refresh")));
            markAllReadButton.style(style -> style.tooltips(
                    Component.translatable("screen.callyou.history.mark_all")));
            doneButton.setText(Component.empty());
            doneButton.style(style -> style.tooltips(
                    Component.translatable("screen.callyou.mention_preferences.done.tooltip")));

            MentionUIStyles.applySwitchStyle(allowMentionsSwitch);
            MentionUIStyles.applySwitchStyle(allowMassMentionsSwitch);

            MentionUIStyles.applyTextFieldStyle(typeSearchInput);
            MentionUIStyles.applyTextFieldStyle(playerSearchInput);

            mentionTypeList.viewContainer(container -> container.layout(layout -> layout.flexDirection(YogaFlexDirection.COLUMN)
                    .gapRow(2)
                    .widthStretch()));
            MentionUIStyles.applyScrollerStyle(mentionTypeList);

            blockedSenderList.viewContainer(container -> container.layout(layout -> layout.flexDirection(YogaFlexDirection.COLUMN)
                    .gapRow(2)
                    .widthStretch()));
            MentionUIStyles.applyScrollerStyle(blockedSenderList);

            historyList.viewContainer(container -> container.layout(layout -> layout.flexDirection(YogaFlexDirection.COLUMN)
                    .gapRow(3)
                    .widthStretch()));
            MentionUIStyles.applyScrollerStyle(historyList);

            blockStatusLabel.setText(Component.empty());
            toastLabel.setText(Component.empty());
            toastLayer.style(style -> style.background(Sprites.RECT_RD_LIGHT));

            modularUI = ModularUI.of(ui);
        }

        private static <T extends UIElement> T require(@NotNull UI ui, @NotNull String selector, @NotNull Class<T> type) {
            return ui.select(selector, type)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Missing UI element: " + selector));
        }
    }
}
