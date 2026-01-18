package cn.qihuang02.callyou.core.client.screen.row;

import cn.qihuang02.callyou.compat.ftb.FTBChunksAPIWrapper;
import cn.qihuang02.callyou.core.client.screen.MentionPreferencesScreen;
import cn.qihuang02.callyou.core.client.screen.MentionUIStyles;
import cn.qihuang02.callyou.core.mention.ComponentTraversal;
import cn.qihuang02.callyou.core.saveddata.MentionRecord;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextField;
import com.lowdragmc.lowdraglib2.gui.ui.style.LayoutStyle;
import com.lowdragmc.lowdraglib2.gui.ui.styletemplate.Sprites;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import org.appliedenergistics.yoga.YogaAlign;
import org.appliedenergistics.yoga.YogaFlexDirection;
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
    private static final Component DELETE_LABEL = Component.literal("X");
    private static final Component READ_LABEL = Component.translatable("screen.callyou.history.mark_read");
    private static final Component COORDS_LABEL = Component.translatable("screen.callyou.history.coords");
    private static final Component REPLY_LABEL = Component.translatable("screen.callyou.history.reply");

    private final UIElement element;

    public MentionHistoryRow(
            @NotNull MentionRecord record,
            @NotNull Runnable onDelete,
            @NotNull Runnable onMarkRead
    ) {
        boolean isRead = record.read();

        String headerText = "[" + DATE_FORMATTER.format(Instant.ofEpochMilli(record.timestamp())) + "] "
                + resolveSenderName(record.senderId(), record.senderName());

        Label header = new Label();
        header.setText(Component.literal(headerText));
        header.layout(style -> style.flexGrow(1));
        header.textStyle(style -> style.textColor(isRead ? 0xDDDDDD : 0xFFE2A0));

        Button deleteButton = new Button();
        deleteButton.setText(DELETE_LABEL);
        deleteButton.layout(style -> style.width(14).height(14));
        MentionUIStyles.applyButtonStyle(deleteButton);
        deleteButton.setOnClick(event -> onDelete.run());

        UIElement headerRow = new UIElement()
                .layout(style -> style.flexDirection(YogaFlexDirection.ROW)
                        .gapColumn(4)
                        .widthStretch()
                        .alignItems(YogaAlign.CENTER))
                .addChildren(header, deleteButton);

        Label body = new MentionHistoryBodyLabel();
        body.setText(record.message());
        body.layout(LayoutStyle::widthStretch);
        body.textStyle(style -> style.textColor(isRead ? 0xCCCCCC : 0xFFFFFF));

        UIElement actionRow = new UIElement()
                .layout(style -> style.flexDirection(YogaFlexDirection.ROW)
                        .gapColumn(4)
                        .widthStretch()
                        .alignItems(YogaAlign.CENTER)
                        .justifyItems(YogaJustify.FLEX_END));

        String replyTrigger = findReplySuggestion(record.message());
        String replySuggestion = replyTrigger != null
                ? buildSenderReplySuggestion(record.senderId(), record.senderName())
                : null;
        UIElement[] replyGroup = new UIElement[1];
        boolean[] replyVisible = new boolean[]{false};

        String waypointCommand = findTransientWaypointCommand(record.message());
        if (shouldShowCoordsButton(waypointCommand)) {
            Button coordsButton = new Button();
            coordsButton.setText(COORDS_LABEL);
            coordsButton.layout(style -> style.width(70).height(14));
            MentionUIStyles.applyButtonStyle(coordsButton);
            coordsButton.setOnClick(event -> createTransientWaypoint(waypointCommand));
            actionRow.addChildren(coordsButton);
        }

        if (replySuggestion != null) {
            TextField replyInput = new TextField();
            replyInput.layout(style -> style.flexGrow(1).height(16));
            MentionUIStyles.applyTextFieldStyle(replyInput);

            Button sendButton = new Button();
            sendButton.setText(Component.literal(">"));
            sendButton.layout(style -> style.width(16).height(16));
            MentionUIStyles.applyButtonStyle(sendButton);

            replyGroup[0] = new UIElement()
                    .layout(style -> style.flexDirection(YogaFlexDirection.ROW)
                            .gapColumn(4)
                            .widthStretch()
                            .alignItems(YogaAlign.CENTER))
                    .addChildren(replyInput, sendButton);
            replyGroup[0].setVisible(false);
            replyGroup[0].setDisplay(false);

            Button replyButton = new Button();
            replyButton.setText(REPLY_LABEL);
            replyButton.layout(style -> style.width(70).height(14));
            MentionUIStyles.applyButtonStyle(replyButton);
            replyButton.setOnClick(event -> {
                boolean newState = !replyVisible[0];
                replyVisible[0] = newState;
                replyGroup[0].setVisible(newState);
                replyGroup[0].setDisplay(newState);
                if (newState) {
                    String current = replyInput.getText();
                    if ((current == null || current.isBlank()) && !replySuggestion.isBlank()) {
                        replyInput.setText(replySuggestion);
                    }
                }
            });
            actionRow.addChildren(replyButton);

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
                replyGroup[0].setVisible(false);
                replyGroup[0].setDisplay(false);
                replyVisible[0] = false;
            });
        }

        Button readButton = new Button();
        readButton.setText(READ_LABEL);
        readButton.layout(style -> style.width(70).height(14));
        MentionUIStyles.applyButtonStyle(readButton);
        readButton.setActive(!isRead);
        readButton.setOnClick(event -> onMarkRead.run());
        actionRow.addChildren(readButton);

        UIElement element = new UIElement()
                .layout(style -> style.flexDirection(YogaFlexDirection.COLUMN)
                        .gapRow(2)
                        .widthStretch()
                        .paddingAll(4))
                .style(style -> style.background(isRead ? Sprites.RECT_RD : Sprites.RECT_RD_LIGHT))
                .addChildren(headerRow, body, actionRow);
        if (replyGroup[0] != null) {
            element.addChild(replyGroup[0]);
        }
        this.element = element;
    }

    public UIElement getElement() {
        return element;
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
