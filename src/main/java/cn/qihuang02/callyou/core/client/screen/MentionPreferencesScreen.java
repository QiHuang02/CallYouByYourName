package cn.qihuang02.callyou.core.client.screen;

import cn.qihuang02.callyou.core.attachment.MentionPreferences;
import cn.qihuang02.callyou.core.client.ClientMentionHistory;
import cn.qihuang02.callyou.core.client.ClientMentionPreferences;
import cn.qihuang02.callyou.core.network.NetworkHandler;
import cn.qihuang02.callyou.core.network.payload.c2s.MentionLogActionPayload;
import cn.qihuang02.callyou.core.saveddata.MentionRecord;
import cn.qihuang02.callyou.registry.CallYouMentionRegistries;
import com.lowdragmc.lowdraglib2.gui.holder.ModularUIScreen;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.elements.*;
import com.lowdragmc.lowdraglib2.gui.ui.style.LayoutStyle;
import com.lowdragmc.lowdraglib2.gui.ui.style.Stylesheet;
import com.lowdragmc.lowdraglib2.gui.ui.style.StylesheetManager;
import com.lowdragmc.lowdraglib2.gui.ui.styletemplate.Sprites;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.appliedenergistics.yoga.YogaAlign;
import org.appliedenergistics.yoga.YogaFlexDirection;
import org.appliedenergistics.yoga.YogaJustify;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MentionPreferencesScreen extends ModularUIScreen {
    private static final int PANEL_WIDTH = 280;
    private static final int PANEL_HEIGHT = 220;

    private static final Component TITLE = Component.translatable("screen.callyou.mention_preferences.title");

    private static final Component ALLOW_MENTIONS = Component.translatable("screen.callyou.mention_preferences.allow_all");
    private static final Component ALLOW_MASS = Component.translatable("screen.callyou.mention_preferences.allow_mass");

    private static final Component EMPTY_BLOCKED = Component.translatable("screen.callyou.mention_preferences.blocked.none");
    private static final Component BLOCK_LABEL = Component.translatable("screen.callyou.blocked_senders.block_label");
    private static final Component BLOCK_ACTION = Component.translatable("screen.callyou.blocked_senders.block_action");
    private static final Component BLOCK_ACTION_TOOLTIP = Component.translatable("screen.callyou.blocked_senders.block_action.tooltip");
    private static final Component ERROR_NOT_FOUND = Component.translatable("screen.callyou.blocked_senders.error.not_found");
    private static final Component ERROR_ALREADY_BLOCKED = Component.translatable("screen.callyou.blocked_senders.error.already_blocked");
    private static final Component TAB_HISTORY = Component.translatable("screen.callyou.history.title");
    private static final Component HISTORY_EMPTY = Component.translatable("screen.callyou.history.empty");
    private static final Component HISTORY_LOADING = Component.translatable("screen.callyou.history.loading");
    private static final Component HISTORY_REFRESH = Component.translatable("screen.callyou.history.refresh");
    private static final Component HISTORY_MARK_ALL = Component.translatable("screen.callyou.history.mark_all");

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
    private MentionPreferences workingCopy;
    private List<MentionRecord> cachedHistory = new ArrayList<>();
    private Tab currentTab = Tab.GENERAL;
    private long lastUiRefreshTime = -1;
    private long lastHistoryRefreshTime = -1;

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
        Label titleLabel;
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

        UIBuilder() {
            build();
        }

        private void build() {
            // --- Header & Tabs ---
            titleLabel = new Label();
            titleLabel.setText(TITLE);
            titleLabel.textStyle(style -> style.textAlignHorizontal(Horizontal.CENTER));
            titleLabel.layout(style -> style.flexGrow(1));

            tabGeneralButton = new Button();
            tabGeneralButton.setText(Component.translatable("screen.callyou.mention_preferences.title"));
            tabGeneralButton.layout(style -> style.flexGrow(1).height(20));
            MentionUIStyles.applyButtonStyle(tabGeneralButton);

            tabBlockedButton = new Button();
            tabBlockedButton.setText(Component.translatable("screen.callyou.blocked_senders.title"));
            tabBlockedButton.layout(style -> style.flexGrow(1).height(20));
            MentionUIStyles.applyButtonStyle(tabBlockedButton);

            tabHistoryButton = new Button();
            tabHistoryButton.setText(TAB_HISTORY);
            tabHistoryButton.layout(style -> style.flexGrow(1).height(20));
            MentionUIStyles.applyButtonStyle(tabHistoryButton);

            UIElement tabBar = new UIElement()
                    .layout(style -> style.flexDirection(YogaFlexDirection.ROW)
                            .gapColumn(4)
                            .widthStretch())
                    .addChildren(tabGeneralButton, tabBlockedButton, tabHistoryButton);

            UIElement header = new UIElement()
                    .layout(style -> style.flexDirection(YogaFlexDirection.COLUMN)
                            .gapRow(4)
                            .widthStretch())
                    .addChildren(titleLabel, tabBar);

            // --- General Tab Content ---
            generalTabContainer = buildGeneralTab();

            // --- Blocked Tab Content ---
            blockedTabContainer = buildBlockedTab();

            // --- History Tab Content ---
            historyTabContainer = buildHistoryTab();

            // --- Content Wrapper ---
            UIElement contentArea = new UIElement()
                    .layout(style -> style.flexGrow(1).widthStretch())
                    .addChildren(generalTabContainer, blockedTabContainer, historyTabContainer);

            // --- Footer ---
            doneButton = new Button();
            doneButton.setText(CommonComponents.GUI_DONE);
            doneButton.layout(style -> style.width(100).height(16));
            MentionUIStyles.applyButtonStyle(doneButton);

            UIElement footer = new UIElement()
                    .layout(style -> style.flexDirection(YogaFlexDirection.ROW)
                            .alignItems(YogaAlign.CENTER)
                            .justifyItems(YogaJustify.CENTER)
                            .height(20)
                            .widthStretch())
                    .addChildren(doneButton);

            // --- Root ---
            UIElement root = new UIElement()
                    .layout(style -> style.width(PANEL_WIDTH)
                            .height(PANEL_HEIGHT)
                            .flexDirection(YogaFlexDirection.COLUMN)
                            .alignItems(YogaAlign.STRETCH)
                            .paddingAll(5)
                            .gapAll(4))
                    .style(style -> style.background(Sprites.RECT_SOLID))
                    .addChildren(header, contentArea, footer);

            Stylesheet mcStyle = StylesheetManager.INSTANCE.getStylesheetSafe(StylesheetManager.MC);
            modularUI = ModularUI.of(UI.of(root, mcStyle));
        }

        private @NotNull UIElement buildGeneralTab() {
            allowMentionsSwitch = new Switch();
            allowMentionsSwitch.layout(style -> style.width(34).height(14));
            MentionUIStyles.applySwitchStyle(allowMentionsSwitch);

            Label allowLabel = new Label();
            allowLabel.setText(ALLOW_MENTIONS);
            allowLabel.layout(style -> style.flexGrow(1));

            UIElement allowRow = new UIElement()
                    .layout(style -> style.flexDirection(YogaFlexDirection.ROW).alignItems(YogaAlign.CENTER).widthStretch())
                    .addChildren(allowLabel, allowMentionsSwitch);

            allowMassMentionsSwitch = new Switch();
            allowMassMentionsSwitch.layout(style -> style.width(34).height(14));
            MentionUIStyles.applySwitchStyle(allowMassMentionsSwitch);

            Label allowMassLabel = new Label();
            allowMassLabel.setText(ALLOW_MASS);
            allowMassLabel.layout(style -> style.flexGrow(1));

            UIElement allowMassRow = new UIElement()
                    .layout(style -> style.flexDirection(YogaFlexDirection.ROW).alignItems(YogaAlign.CENTER).widthStretch())
                    .addChildren(allowMassLabel, allowMassMentionsSwitch);

            typeSearchInput = new TextField();
            typeSearchInput.layout(style -> style.widthStretch().height(16));
            MentionUIStyles.applyTextFieldStyle(typeSearchInput);

            mentionTypeList = new ScrollerView();
            mentionTypeList.layout(style -> style.flexGrow(1).widthStretch());
            mentionTypeList.viewContainer(container -> container.layout(layout -> layout.flexDirection(YogaFlexDirection.COLUMN)
                    .gapRow(2)
                    .widthStretch()));
            MentionUIStyles.applyScrollerStyle(mentionTypeList);

            return new UIElement()
                    .layout(style -> style.flexDirection(YogaFlexDirection.COLUMN).gapRow(4).flexGrow(1).widthStretch())
                    .addChildren(allowRow, allowMassRow, typeSearchInput, mentionTypeList);
        }

        private @NotNull UIElement buildBlockedTab() {
            Label blockLabel = new Label();
            blockLabel.setText(BLOCK_LABEL);

            playerSearchInput = new TextField();
            playerSearchInput.layout(style -> style.flexGrow(1).height(16));
            MentionUIStyles.applyTextFieldStyle(playerSearchInput);

            blockButton = new Button();
            blockButton.setText(BLOCK_ACTION);
            blockButton.layout(style -> style.width(50).height(16));
            MentionUIStyles.applyButtonStyle(blockButton);

            UIElement searchRow = new UIElement()
                    .layout(style -> style.flexDirection(YogaFlexDirection.ROW).alignItems(YogaAlign.CENTER).gapColumn(4).widthStretch())
                    .addChildren(playerSearchInput, blockButton);

            blockStatusLabel = new Label();
            blockStatusLabel.setText(Component.empty());
            blockStatusLabel.layout(LayoutStyle::widthStretch);

            blockedSenderList = new ScrollerView();
            blockedSenderList.layout(style -> style.flexGrow(1).widthStretch());
            blockedSenderList.viewContainer(container -> container.layout(layout -> layout.flexDirection(YogaFlexDirection.COLUMN)
                    .gapRow(2)
                    .widthStretch()));
            MentionUIStyles.applyScrollerStyle(blockedSenderList);

            blockedEmptyLabel = new Label();
            blockedEmptyLabel.setText(EMPTY_BLOCKED);
            blockedEmptyLabel.textStyle(style -> style.textAlignHorizontal(Horizontal.CENTER).textColor(0xAAAAAA));
            blockedEmptyLabel.layout(style -> style.widthStretch().height(20));
            blockedEmptyLabel.setVisible(false);

            return new UIElement()
                    .layout(style -> style.flexDirection(YogaFlexDirection.COLUMN).gapRow(4).flexGrow(1).widthStretch())
                    .addChildren(blockLabel, searchRow, blockStatusLabel, blockedSenderList);
        }

        private @NotNull UIElement buildHistoryTab() {
            historyRefreshButton = new Button();
            historyRefreshButton.setText(HISTORY_REFRESH);
            historyRefreshButton.layout(style -> style.width(70).height(16));
            MentionUIStyles.applyButtonStyle(historyRefreshButton);

            markAllReadButton = new Button();
            markAllReadButton.setText(HISTORY_MARK_ALL);
            markAllReadButton.layout(style -> style.width(100).height(16));
            MentionUIStyles.applyButtonStyle(markAllReadButton);

            UIElement actionsRow = new UIElement()
                    .layout(style -> style.flexDirection(YogaFlexDirection.ROW)
                            .gapColumn(4)
                            .widthStretch())
                    .addChildren(historyRefreshButton, markAllReadButton);

            historyStatusLabel = new Label();
            historyStatusLabel.setText(HISTORY_EMPTY);
            historyStatusLabel.layout(LayoutStyle::widthStretch);

            historyList = new ScrollerView();
            historyList.layout(style -> style.flexGrow(1).widthStretch());
            historyList.viewContainer(container -> container.layout(layout -> layout.flexDirection(YogaFlexDirection.COLUMN)
                    .gapRow(3)
                    .widthStretch()));
            MentionUIStyles.applyScrollerStyle(historyList);

            historyEmptyLabel = new Label();
            historyEmptyLabel.setText(HISTORY_EMPTY);
            historyEmptyLabel.textStyle(style -> style.textAlignHorizontal(Horizontal.CENTER).textColor(0xAAAAAA));
            historyEmptyLabel.layout(style -> style.widthStretch().height(20));
            historyEmptyLabel.setVisible(false);

            return new UIElement()
                    .layout(style -> style.flexDirection(YogaFlexDirection.COLUMN).gapRow(4).flexGrow(1).widthStretch())
                    .addChildren(actionsRow, historyStatusLabel, historyList);
        }
    }
}
