package cn.qihuang02.callyou.client;

import cn.qihuang02.callyou.attachment.MentionPreferences;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class BlockedSendersScreen extends Screen {
    private static final Component TITLE = Component.translatable("screen.callyou.blocked_senders.title");
    private static final Component UNBLOCK = Component.translatable("screen.callyou.blocked_senders.unblock");
    private static final Component EMPTY = Component.translatable("screen.callyou.mention_preferences.blocked.none");

    private final MentionPreferencesScreen parent;
    private final MentionPreferences preferences;
    private final boolean controlsActive;

    private SenderList senderList;

    protected BlockedSendersScreen(@NotNull MentionPreferencesScreen parent, @NotNull MentionPreferences preferences, boolean controlsActive) {
        super(TITLE);
        this.parent = Objects.requireNonNull(parent);
        this.preferences = Objects.requireNonNull(preferences);
        this.controlsActive = controlsActive;
    }

    @Override
    protected void init() {
        super.init();

        this.senderList = this.addRenderableWidget(new SenderList(this.minecraft, this.width, this.height - 70, 40, 32));
        this.senderList.reload(this.preferences.getBlockedSenders());

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
                .bounds(this.width / 2 - 50, this.height - 30, 100, 20)
                .build());
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 12, 0xFFFFFF);

        super.render(graphics, mouseX, mouseY, partialTick);

        if (this.senderList != null && this.senderList.children().isEmpty()) {
            graphics.drawCenteredString(this.font, EMPTY, this.width / 2, this.height / 2 - 10, 0xAAAAAA);
        }
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
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

    class SenderEntry extends ObjectSelectionList.Entry<SenderEntry> {
        private final UUID senderId;
        private final Component displayName;
        private final Button unblockButton;

        SenderEntry(UUID senderId) {
            this.senderId = Objects.requireNonNull(senderId);
            this.displayName = resolveDisplayName(senderId);
            this.unblockButton = Button.builder(UNBLOCK, button -> {
                        preferences.unblockSender(this.senderId);
                        parent.onBlockedSendersChanged();
                        if (senderList != null) {
                            senderList.reload(preferences.getBlockedSenders());
                        }
                    })
                    .bounds(0, 0, 80, 20)
                    .build();
            this.unblockButton.active = controlsActive;
        }

        @Override
        public @NotNull Component getNarration() {
            return this.displayName;
        }

        @Override
        public void render(@NotNull GuiGraphics graphics, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float partialTick) {
            graphics.drawString(BlockedSendersScreen.this.font, this.displayName, x + 4, y + 6, 0xFFFFFF, false);
            graphics.drawString(BlockedSendersScreen.this.font, this.senderId.toString(), x + 4, y + 16, 0x888888, false);
            this.unblockButton.setX(x + entryWidth - 90);
            this.unblockButton.setY(y + (entryHeight - 20) / 2);
            this.unblockButton.render(graphics, mouseX, mouseY, partialTick);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return this.unblockButton.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            return this.unblockButton.mouseReleased(mouseX, mouseY, button);
        }
    }

    class SenderList extends ObjectSelectionList<SenderEntry> {
        SenderList(Minecraft minecraft, int width, int height, int top, int itemHeight) {
            super(minecraft, width, height, top, itemHeight);
        }

        void reload(@NotNull Iterable<UUID> ids) {
            List<UUID> sorted = new ArrayList<>();
            ids.forEach(sorted::add);
            sorted.sort(Comparator.comparing(uuid -> resolveDisplayName(uuid).getString(), String.CASE_INSENSITIVE_ORDER));

            this.clearEntries();
            for (UUID id : sorted) {
                this.addEntry(new SenderEntry(id));
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
