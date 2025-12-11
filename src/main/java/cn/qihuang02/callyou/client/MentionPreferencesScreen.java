package cn.qihuang02.callyou.client;

import cn.qihuang02.callyou.attachment.MentionPreferences;
import cn.qihuang02.callyou.network.CallYouNetwork;
import cn.qihuang02.callyou.registry.CallYouMentionRegistries;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MentionPreferencesScreen extends Screen {
    private static final int LIST_TOP_OFFSET = 85;
    private static final int LIST_BOTTOM_OFFSET = 70;
    private static final Component TITLE = Component.translatable("screen.callyou.mention_preferences.title");
    private static final Component ALLOW_MENTIONS = Component.translatable("screen.callyou.mention_preferences.allow_all");
    private static final Component ALLOW_MASS = Component.translatable("screen.callyou.mention_preferences.allow_mass");
    private static final Component TYPE_LABEL = Component.translatable("screen.callyou.mention_preferences.type_label");
    private static final Component MANAGE_BLOCKED = Component.translatable("screen.callyou.mention_preferences.manage_blocked");

    private final Screen parent;
    private MentionPreferences workingCopy;
    private MentionTypeList mentionTypeList;
    private CycleButton<Boolean> allowMentionsButton;
    private CycleButton<Boolean> allowMassMentionsButton;
    private Button manageBlockedSendersButton;
    private Component statusMessage = Component.empty();
    private int statusColor = 0xAAAAAA;
    private long lastSeenSyncMillis;
    private boolean lastPendingState;
    private boolean awaitingSync;
    private boolean controlsActive;

    public MentionPreferencesScreen(@Nullable Screen parent) {
        super(TITLE);
        this.parent = parent;
    }

    public static MentionPreferencesScreen createWithRefresh(@Nullable Screen parent) {
        ClientMentionPreferences.markAwaitingSync();
        CallYouNetwork.requestPreferencesSync();
        return new MentionPreferencesScreen(parent);
    }

    @Override
    protected void init() {
        this.requestLatestPreferences();
        this.workingCopy = ClientMentionPreferences.copy();
        this.lastSeenSyncMillis = ClientMentionPreferences.getLastSyncMillis();
        this.lastPendingState = ClientMentionPreferences.isSyncPending();
        this.awaitingSync = ClientMentionPreferences.isAwaitingSync() && this.lastSeenSyncMillis == 0;
        this.controlsActive = this.lastSeenSyncMillis > 0;

        int centerX = this.width / 2;
        int y = 30;

        this.allowMentionsButton = addRenderableWidget(CycleButton.onOffBuilder(this.workingCopy.isAllowMentions())
                .withTooltip(value -> Tooltip.create(Component.translatable("screen.callyou.mention_preferences.allow_all.tooltip")))
                .create(centerX - 100, y, 200, 20, ALLOW_MENTIONS, (button, value) -> {
                    this.workingCopy.setAllowMentions(value);
                    this.refreshTypeButtons();
                    this.sendUpdate();
                }));
        this.allowMentionsButton.active = this.controlsActive;

        y += 28;

        this.allowMassMentionsButton = addRenderableWidget(CycleButton.onOffBuilder(this.workingCopy.isAllowMassMentions())
                .withTooltip(value -> Tooltip.create(Component.translatable("screen.callyou.mention_preferences.allow_mass.tooltip")))
                .create(centerX - 100, y, 200, 20, ALLOW_MASS, (button, value) -> {
                    this.workingCopy.setAllowMassMentions(value);
                    this.sendUpdate();
                }));
        this.allowMassMentionsButton.active = this.controlsActive;

        this.mentionTypeList = addRenderableWidget(new MentionTypeList(this.minecraft, this.width, this.height - LIST_TOP_OFFSET - LIST_BOTTOM_OFFSET, LIST_TOP_OFFSET, 26));
        this.populateMentionTypeList();

        this.manageBlockedSendersButton = addRenderableWidget(Button.builder(MANAGE_BLOCKED, button -> {
                    if (this.minecraft != null && this.controlsActive) {
                        this.minecraft.setScreen(new BlockedSendersScreen(this, this.workingCopy, this.controlsActive));
                    }
                })
                .bounds(centerX - 155, this.height - 40, 150, 20)
                .build());
        this.manageBlockedSendersButton.active = this.controlsActive;

        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
                .bounds(centerX + 5, this.height - 40, 100, 20)
                .build());

        this.updateStatus();
    }

    @Override
    public void tick() {
        super.tick();
        long previousSync = this.lastSeenSyncMillis;
        long currentSync = ClientMentionPreferences.getLastSyncMillis();
        boolean pending = ClientMentionPreferences.isSyncPending();

        if (currentSync > previousSync) {
            this.lastSeenSyncMillis = currentSync;
            this.workingCopy = ClientMentionPreferences.copy();
            this.refreshTypeButtons();
            this.allowMentionsButton.setValue(this.workingCopy.isAllowMentions());
            this.allowMassMentionsButton.setValue(this.workingCopy.isAllowMassMentions());
            this.awaitingSync = false;
            this.controlsActive = true;
            this.updateControlState();
        }

        if (pending != this.lastPendingState || currentSync > previousSync) {
            this.lastPendingState = pending;
            this.updateStatus();
        }

        boolean awaiting = ClientMentionPreferences.isAwaitingSync() && currentSync == 0;
        if (this.awaitingSync != awaiting) {
            this.awaitingSync = awaiting;
            this.controlsActive = currentSync > 0;
            this.updateControlState();
            this.updateStatus();
        }
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 10, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);

        graphics.drawString(this.font, TYPE_LABEL, this.width / 2 - 100, LIST_TOP_OFFSET - 12, 0xFFFFFF, false);

        graphics.drawString(this.font, this.statusMessage, 20, this.height - 20, this.statusColor, false);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    private void populateMentionTypeList() {
        List<ResourceLocation> mentionTypes = this.resolveMentionTypes();
        this.mentionTypeList.reload(mentionTypes);
        this.refreshTypeButtons();
    }

    private List<ResourceLocation> resolveMentionTypes() {
        Minecraft mc = this.minecraft;
        if (mc == null || mc.level == null) {
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

    private void refreshTypeButtons() {
        if (this.mentionTypeList == null) {
            return;
        }
        boolean allowMentions = this.workingCopy.isAllowMentions();
        for (MentionTypeEntry entry : this.mentionTypeList.children()) {
            entry.updateState(this.controlsActive && allowMentions, !this.workingCopy.getBlockedTypes().contains(entry.id));
        }
    }

    private void sendUpdate() {
        MentionPreferences payload = new MentionPreferences();
        payload.copyFrom(this.workingCopy);
        ClientMentionPreferences.markPending(payload);
        CallYouNetwork.sendPreferenceUpdate(payload);
        this.updateStatus();
    }

    private void updateStatus() {
        if (this.awaitingSync && this.lastSeenSyncMillis == 0) {
            this.statusMessage = Component.translatable("screen.callyou.mention_preferences.status.loading").withStyle(ChatFormatting.YELLOW);
            this.statusColor = colorOf(ChatFormatting.YELLOW);
            return;
        }
        if (ClientMentionPreferences.isSyncPending()) {
            this.statusMessage = Component.translatable("screen.callyou.mention_preferences.status.pending").withStyle(ChatFormatting.GOLD);
            this.statusColor = colorOf(ChatFormatting.GOLD);
            return;
        }
        if (ClientMentionPreferences.getLastSyncMillis() > 0) {
            this.statusMessage = Component.translatable("screen.callyou.mention_preferences.status.synced").withStyle(ChatFormatting.GREEN);
            this.statusColor = colorOf(ChatFormatting.GREEN);
            return;
        }
        this.statusMessage = Component.translatable("screen.callyou.mention_preferences.status.unknown").withStyle(ChatFormatting.RED);
        this.statusColor = colorOf(ChatFormatting.RED);
    }

    private void requestLatestPreferences() {
        if (!ClientMentionPreferences.isAwaitingSync()) {
            ClientMentionPreferences.markAwaitingSync();
            CallYouNetwork.requestPreferencesSync();
        }
    }

    void onBlockedSendersChanged() {
        this.sendUpdate();
        this.updateStatus();
    }

    boolean areControlsActive() {
        return this.controlsActive;
    }

    private void updateControlState() {
        if (this.allowMentionsButton != null) {
            this.allowMentionsButton.active = this.controlsActive;
        }
        if (this.allowMassMentionsButton != null) {
            this.allowMassMentionsButton.active = this.controlsActive;
        }
        if (this.manageBlockedSendersButton != null) {
            this.manageBlockedSendersButton.active = this.controlsActive;
        }
        this.refreshTypeButtons();
    }

    @Contract(pure = true)
    private static int colorOf(@NotNull ChatFormatting formatting) {
        Integer color = formatting.getColor();
        return color != null ? color : 0xFFFFFF;
    }

    class MentionTypeEntry extends ObjectSelectionList.Entry<MentionTypeEntry> {
        private final ResourceLocation id;
        private final CycleButton<Boolean> toggle;

        MentionTypeEntry(ResourceLocation id) {
            this.id = Objects.requireNonNull(id);
            boolean enabled = !workingCopy.getBlockedTypes().contains(id);
            this.toggle = CycleButton.onOffBuilder(enabled)
                    .displayOnlyValue()
                    .create(0, 0, 80, 20, Component.literal(id.toString()), (button, value) -> {
                        if (value) {
                            workingCopy.unblockMentionType(this.id);
                        } else {
                            workingCopy.blockMentionType(this.id);
                        }
                        sendUpdate();
                    });
        }

        void updateState(boolean allowMentions, boolean enabled) {
            this.toggle.active = allowMentions;
            this.toggle.setValue(enabled);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return this.toggle.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            return this.toggle.mouseReleased(mouseX, mouseY, button);
        }

        @Override
        public @NotNull Component getNarration() {
            return Component.literal(this.id.toString());
        }

        @Override
        public void render(@NotNull GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float partialTick) {
            graphics.drawString(MentionPreferencesScreen.this.font, this.id.toString(), x + 4, y + 6, 0xFFFFFF, false);
            this.toggle.setX(x + entryWidth - 90);
            this.toggle.setY(y + (entryHeight - 20) / 2);
            this.toggle.render(graphics, mouseX, mouseY, partialTick);
        }
    }

    class MentionTypeList extends ObjectSelectionList<MentionPreferencesScreen.MentionTypeEntry> {
        MentionTypeList(Minecraft minecraft, int width, int height, int top, int itemHeight) {
            super(minecraft, width, height, top, itemHeight);
        }

        void reload(@NotNull List<ResourceLocation> ids) {
            this.clearEntries();
            for (ResourceLocation id : ids) {
                this.addEntry(new MentionTypeEntry(id));
            }
        }

        @Override
        protected int getScrollbarPosition() {
            return this.width - 6;
        }

        @Override
        public int getRowWidth() {
            return this.width - 24;
        }
    }
}
