package cn.qihuang02.callyou.core.client.screen.row;

import cn.qihuang02.callyou.compat.ftb.FTBChunksAPIWrapper;
import cn.qihuang02.callyou.core.client.screen.MentionPreferencesScreen;
import cn.qihuang02.callyou.core.client.screen.MentionUIStyles;
import cn.qihuang02.callyou.core.saveddata.MentionRecord;
import cn.qihuang02.callyou.util.ComponentTraversal;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.UITemplate;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextField;
import com.lowdragmc.lowdraglib2.gui.ui.style.LayoutStyle;
import com.lowdragmc.lowdraglib2.gui.ui.styletemplate.Sprites;
import com.lowdragmc.lowdraglib2.utils.XmlUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.appliedenergistics.yoga.YogaJustify;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class MentionHistoryRow {
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("MM-dd HH:mm").withZone(ZoneId.systemDefault());
    private static final Map<UUID, String> LAST_KNOWN_SENDER_NAMES = new ConcurrentHashMap<>();
    private static final Map<UUID, String> LAST_KNOWN_SENDER_PROFILES = new ConcurrentHashMap<>();
    private static final ResourceLocation UI_XML = ResourceLocation.parse("callyou:ui/mention_history_row.xml");
    private static UITemplate template;

    private final UIElement element;

    public MentionHistoryRow(
            @NotNull MentionRecord record,
            @NotNull Runnable onDelete,
            @NotNull Runnable onMarkRead
    ) {
        boolean isRead = record.read();

        UI ui = template().createUI();
        UIElement root = ui.getRootElement();
        Label header = require(ui, "#history-header", Label.class);
        Button deleteButton = require(ui, "#history-delete", Button.class);
        UIElement bodySlot = require(ui, "#history-body-slot", UIElement.class);
        UIElement actionRow = require(ui, "#history-action-row", UIElement.class);
        Button coordsButton = require(ui, "#history-coords", Button.class);
        Button replyButton = require(ui, "#history-reply", Button.class);
        Button readButton = require(ui, "#history-read", Button.class);
        UIElement replyGroup = require(ui, "#history-reply-group", UIElement.class);
        TextField replyInput = require(ui, "#history-reply-input", TextField.class);
        Button sendButton = require(ui, "#history-reply-send", Button.class);

        String headerText = "[" + DATE_FORMATTER.format(Instant.ofEpochMilli(record.timestamp())) + "] "
                + resolveSenderName(record.senderId(), record.senderName());

        header.setText(Component.literal(headerText));
        header.textStyle(style -> style.textColor(isRead ? 0xDDDDDD : 0xFFE2A0));

        MentionUIStyles.applyButtonStyle(deleteButton);
        deleteButton.setText(Component.empty());
        deleteButton.setOnClick(event -> onDelete.run());

        Label body = new MentionHistoryBodyLabel();
        body.setText(record.message());
        body.layout(LayoutStyle::widthStretch);
        body.textStyle(style -> style.textColor(isRead ? 0xCCCCCC : 0xFFFFFF));
        bodySlot.addChild(body);
        actionRow.layout(layout -> layout.justifyItems(YogaJustify.FLEX_END));

        String replyTrigger = findReplySuggestion(record.message());
        String replySuggestion = replyTrigger != null
                ? buildSenderReplySuggestion(record.senderId(), record.senderName())
                : null;
        boolean[] replyVisible = new boolean[]{false};

        String waypointCommand = findTransientWaypointCommand(record.message());
        if (shouldShowCoordsButton(waypointCommand)) {
            MentionUIStyles.applyButtonStyle(coordsButton);
            coordsButton.setText(Component.empty());
            coordsButton.setOnClick(event -> createTransientWaypoint(waypointCommand));
            coordsButton.setVisible(true);
            coordsButton.setDisplay(true);
        } else {
            coordsButton.setVisible(false);
            coordsButton.setDisplay(false);
        }

        if (replySuggestion != null) {
            MentionUIStyles.applyTextFieldStyle(replyInput);

            MentionUIStyles.applyButtonStyle(sendButton);
            sendButton.setText(Component.empty());

            MentionUIStyles.applyButtonStyle(replyButton);
            replyButton.setText(Component.empty());
            replyButton.style(style -> style.tooltips(Component.translatable("screen.callyou.history.reply")));
            replyButton.setOnClick(event -> {
                boolean newState = !replyVisible[0];
                replyVisible[0] = newState;
                replyGroup.setVisible(newState);
                replyGroup.setDisplay(newState);
                if (newState) {
                    String current = replyInput.getText();
                    if ((current == null || current.isBlank()) && !replySuggestion.isBlank()) {
                        replyInput.setText(replySuggestion);
                    }
                }
            });

            sendButton.setOnClick(event -> {
                String content = replyInput.getText();
                if (content == null || content.isBlank()) {
                    return;
                }
                Minecraft minecraft = Minecraft.getInstance();
                if (minecraft == null || minecraft.player == null) {
                    return;
                }
                minecraft.player.connection.sendChat(content);
                replyInput.setText("");
                replyGroup.setVisible(false);
                replyGroup.setDisplay(false);
                replyVisible[0] = false;
            });
            replyButton.setVisible(true);
            replyButton.setDisplay(true);
        } else {
            replyButton.setVisible(false);
            replyButton.setDisplay(false);
            replyGroup.setVisible(false);
            replyGroup.setDisplay(false);
        }

        MentionUIStyles.applyButtonStyle(readButton);
        readButton.setText(Component.empty());
        readButton.setActive(!isRead);
        readButton.setOnClick(event -> onMarkRead.run());

        root.style(style -> style.background(isRead ? Sprites.RECT_RD : Sprites.RECT_RD_LIGHT));
        this.element = root;
    }

    public UIElement getElement() {
        return element;
    }

    private static @NotNull UITemplate template() {
        if (template == null) {
            var xml = XmlUtils.loadXml(UI_XML);
            if (xml == null) {
                throw new IllegalStateException("UI xml not found: " + UI_XML);
            }
            template = UI.of(xml).toTemplate();
        }
        return template;
    }

    private static <T extends UIElement> @NotNull T require(
            @NotNull UI ui,
            @NotNull String selector,
            @NotNull Class<T> type
    ) {
        return ui.select(selector, type)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Missing UI element: " + selector));
    }

    private void createTransientWaypoint(@NotNull String command) {
        if (!FTBChunksAPIWrapper.isLoaded()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.player == null) {
            return;
        }
        FTBChunksAPIWrapper.handleTransientWaypointCommand(command)
                .ifPresent(name -> {
                    Component message = Component.translatable("message.callyou.spot.ftb.added", name);
                    if (minecraft.screen instanceof MentionPreferencesScreen screen) {
                        screen.showTransientMessage(message);
                        return;
                    }
                    minecraft.player.displayClientMessage(message, true);
                });
    }

    private boolean shouldShowCoordsButton(@Nullable String waypointCommand) {
        return FTBChunksAPIWrapper.isLoaded() && waypointCommand != null;
    }

    private @Nullable String findTransientWaypointCommand(@NotNull Component component) {
        return ComponentTraversal.findFirstStyleValue(
                component,
                MentionHistoryRowUtils::getTransientWaypointCommand
        );
    }

    private @Nullable String findReplySuggestion(@NotNull Component component) {
        return ComponentTraversal.findFirstStyleValue(
                component,
                MentionHistoryRowUtils::getReplySuggestion
        );
    }

    private @NotNull String resolveSenderName(@NotNull UUID senderId, @Nullable String fallbackName) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft != null) {
            ClientPacketListener connection = minecraft.getConnection();
            if (connection != null) {
                PlayerInfo info = connection.getPlayerInfo(senderId);
                if (info != null) {
                    Component displayName = info.getTabListDisplayName();
                    if (displayName != null) {
                        String text = displayName.getString();
                        if (!text.isEmpty()) {
                            LAST_KNOWN_SENDER_NAMES.put(senderId, text);
                            return text;
                        }
                    }
                    String profileName = info.getProfile().getName();
                    if (profileName != null && !profileName.isEmpty()) {
                        LAST_KNOWN_SENDER_NAMES.put(senderId, profileName);
                        LAST_KNOWN_SENDER_PROFILES.put(senderId, profileName);
                        return profileName;
                    }
                }
            }
        }
        String fallback = MentionHistoryRowUtils.normalizeSenderName(fallbackName);
        if (fallback != null) {
            LAST_KNOWN_SENDER_NAMES.put(senderId, fallback);
            LAST_KNOWN_SENDER_PROFILES.put(senderId, fallback);
            return fallback;
        }
        String cached = LAST_KNOWN_SENDER_NAMES.get(senderId);
        return cached != null ? cached : senderId.toString();
    }

    private @Nullable String resolveSenderProfileName(@NotNull UUID senderId, @Nullable String fallbackName) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft != null) {
            ClientPacketListener connection = minecraft.getConnection();
            if (connection != null) {
                PlayerInfo info = connection.getPlayerInfo(senderId);
                if (info != null) {
                    String profileName = info.getProfile().getName();
                    if (profileName != null && !profileName.isEmpty()) {
                        LAST_KNOWN_SENDER_PROFILES.put(senderId, profileName);
                        return profileName;
                    }
                }
            }
        }
        String fallback = MentionHistoryRowUtils.normalizeSenderName(fallbackName);
        if (fallback != null) {
            LAST_KNOWN_SENDER_PROFILES.put(senderId, fallback);
            return fallback;
        }
        return LAST_KNOWN_SENDER_PROFILES.get(senderId);
    }

    private @NotNull String buildSenderReplySuggestion(@NotNull UUID senderId, @Nullable String fallbackName) {
        String senderName = resolveSenderProfileName(senderId, fallbackName);
        if (senderName == null || senderName.isBlank()) {
            return "";
        }
        return "@" + senderName + " ";
    }
}
