package cn.qihuang02.callyou.core.client.screen;

import cn.qihuang02.callyou.compat.ftb.FTBChunksAPIWrapper;
import cn.qihuang02.callyou.core.saveddata.MentionRecord;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextField;
import com.lowdragmc.lowdraglib2.gui.ui.event.HoverTooltips;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.ui.style.LayoutStyle;
import com.lowdragmc.lowdraglib2.gui.ui.styletemplate.Sprites;
import com.lowdragmc.lowdraglib2.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib2.utils.TextUtilities;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import org.appliedenergistics.yoga.YogaAlign;
import org.appliedenergistics.yoga.YogaFlexDirection;
import org.appliedenergistics.yoga.YogaJustify;
import org.appliedenergistics.yoga.YogaPositionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
    private static final String ITEM_ICON_PLACEHOLDER = "  ";
    private static final float ITEM_ICON_EXTRA_SHIFT = 1.0F;
    private static final String SPOT_FTB_ADD_TOOLTIP_KEY = "message.callyou.spot.ftb.add";

    private final UIElement element;

    public MentionHistoryRow(
            @NotNull MentionRecord record,
            @NotNull Runnable onDelete,
            @NotNull Runnable onMarkRead
    ) {
        UUID localPlayerId = resolveLocalPlayerId();
        boolean isRead = record.isRead(localPlayerId);

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

        Label body = new MentionBodyLabel();
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
        String command = getTransientWaypointCommand(component.getStyle());
        if (command != null) {
            return command;
        }
        if (component.getContents() instanceof TranslatableContents translatable) {
            for (Object arg : translatable.getArgs()) {
                if (arg instanceof Component nested) {
                    command = findTransientWaypointCommand(nested);
                    if (command != null) {
                        return command;
                    }
                }
            }
        }
        for (Component sibling : component.getSiblings()) {
            command = findTransientWaypointCommand(sibling);
            if (command != null) {
                return command;
            }
        }
        return null;
    }

    private @Nullable String getTransientWaypointCommand(@NotNull Style style) {
        ClickEvent clickEvent = style.getClickEvent();
        if (clickEvent == null || clickEvent.getAction() != ClickEvent.Action.RUN_COMMAND) {
            return null;
        }
        String value = clickEvent.getValue();
        if (value == null || !value.startsWith(FTBChunksAPIWrapper.TRANSIENT_WAYPOINT_COMMAND)) {
            return null;
        }
        return value;
    }

    private @Nullable String findReplySuggestion(@NotNull Component component) {
        String suggestion = getReplySuggestion(component.getStyle());
        if (suggestion != null) {
            return suggestion;
        }
        if (component.getContents() instanceof TranslatableContents translatable) {
            for (Object arg : translatable.getArgs()) {
                if (arg instanceof Component nested) {
                    suggestion = findReplySuggestion(nested);
                    if (suggestion != null) {
                        return suggestion;
                    }
                }
            }
        }
        for (Component sibling : component.getSiblings()) {
            suggestion = findReplySuggestion(sibling);
            if (suggestion != null) {
                return suggestion;
            }
        }
        return null;
    }

    private @Nullable String getReplySuggestion(@NotNull Style style) {
        ClickEvent clickEvent = style.getClickEvent();
        if (clickEvent == null || clickEvent.getAction() != ClickEvent.Action.SUGGEST_COMMAND) {
            return null;
        }
        String value = clickEvent.getValue();
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }

    private @NotNull UUID resolveLocalPlayerId() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft != null && minecraft.player != null) {
            return minecraft.player.getUUID();
        }
        return Util.NIL_UUID;
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
        String fallback = normalizeSenderName(fallbackName);
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
        String fallback = normalizeSenderName(fallbackName);
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

    private @Nullable String normalizeSenderName(@Nullable String name) {
        if (name == null) {
            return null;
        }
        String trimmed = name.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static final class MentionBodyLabel extends Label {
        private ItemSlot itemSlot;

        private MentionBodyLabel() {
            addEventListener(UIEvents.HOVER_TOOLTIPS, this::onHoverTooltips);
            ensureItemSlot();
            refreshItemSlot();
        }

        @Override
        public @NotNull MentionBodyLabel setText(@NotNull Component text) {
            super.setText(text);
            if (itemSlot != null) {
                refreshItemSlot();
            }
            return this;
        }

        @Override
        protected void onLayoutChanged() {
            super.onLayoutChanged();
            if (itemSlot != null) {
                refreshItemSlot();
            }
        }

        private void ensureItemSlot() {
            if (itemSlot != null) {
                return;
            }
            itemSlot = MentionUIStyles.createItemSlot(ItemStack.EMPTY, 18, false, true);
            itemSlot.layout(style -> style.positionType(YogaPositionType.ABSOLUTE).paddingAll(0));
            itemSlot.setVisible(false);
            addChild(itemSlot);
        }

        private void refreshItemSlot() {
            ensureItemSlot();
            ItemIconPlacement placement = findItemIconPlacement();
            if (placement == null) {
                itemSlot.setVisible(false);
                return;
            }
            float itemSize = Math.max(9.0F, getTextStyle().fontSize());
            float localX = placement.x() - getContentX();
            float localY = placement.y() - getContentY();
            itemSlot.setItem(placement.stack());
            itemSlot.layout(style -> style.positionType(YogaPositionType.ABSOLUTE)
                    .left(localX)
                    .top(localY)
                    .width(itemSize)
                    .height(itemSize)
                    .paddingAll(0));
            itemSlot.setVisible(true);
        }

        private void onHoverTooltips(UIEvent event) {
            HoverEvent hover = findHoverEventAtMouse();
            if (hover == null) {
                return;
            }
            HoverTooltips tooltips = buildHoverTooltips(hover);
            if (tooltips == null || tooltips.tooltipTexts().isEmpty()) {
                return;
            }
            event.hoverTooltips = tooltips;
        }

        private @Nullable HoverEvent findHoverEventAtMouse() {
            var ui = getModularUI();
            if (ui == null) {
                return null;
            }
            return findHoverEventAt(ui.getLastMouseX(), ui.getLastMouseY());
        }

        private @Nullable HoverEvent findHoverEventAt(float mouseX, float mouseY) {
            Component text = getText();
            if (text == null) {
                return null;
            }
            float width = getContentWidth();
            if (width <= 0.0F) {
                return null;
            }

            Font font = getFont();
            float lineHeight = getTextStyle().fontSize();
            float lineSpacing = getTextStyle().lineSpacing();
            float scale = lineHeight / font.lineHeight;

            List<Tuple<FormattedCharSequence, Float>> lines =
                    TextUtilities.computeFormattedLines(font, text, lineHeight, width);
            if (lines.isEmpty()) {
                return null;
            }

            List<Tuple<FormattedCharSequence, Float>> displayLines = lines;
            TextWrap textWrap = getTextStyle().textWrap();
            if (textWrap == TextWrap.HIDE) {
                displayLines = lines.subList(0, Math.min(1, lines.size()));
            }

            float totalTextHeight = displayLines.size() * (lineHeight + lineSpacing) - lineSpacing;
            float startY = getContentY();
            switch (getTextStyle().textAlignVertical()) {
                case TOP -> startY = getContentY();
                case CENTER -> startY = getContentY() + (getContentHeight() - totalTextHeight) / 2;
                case BOTTOM -> startY = getContentY() + (getContentHeight() - totalTextHeight);
            }

            boolean roll = textWrap == TextWrap.ROLL || (textWrap == TextWrap.HOVER_ROLL && isSelfOrChildHover());
            for (Tuple<FormattedCharSequence, Float> tuple : displayLines) {
                FormattedCharSequence line = tuple.getA();
                float lineWidth = tuple.getB();
                float lineX = getContentX();

                if (roll && lineWidth > width) {
                    float rollSpeed = getTextStyle().rollSpeed();
                    float totalW = width + lineWidth + 10;
                    float t = rollSpeed > 0
                            ? (((rollSpeed * Math.abs((int) (System.currentTimeMillis() % 1000000)) / 10)
                            % (totalW)) / (totalW))
                            : 0.5F;
                    lineX = getContentX() + width - totalW * t;
                } else {
                    switch (getTextStyle().textAlignHorizontal()) {
                        case LEFT -> lineX = getContentX();
                        case CENTER -> lineX = (lineWidth > width) ? getContentX()
                                : (getContentX() + (width - lineWidth) / 2);
                        case RIGHT -> lineX = getContentX() + (width - lineWidth);
                    }
                }

                float lineY = startY;
                if (mouseY < lineY || mouseY > lineY + lineHeight) {
                    startY += lineHeight + lineSpacing;
                    continue;
                }

                HoverEvent hover = findHoverEventInLine(font, line, lineX, lineWidth, scale, mouseX);
                if (hover != null) {
                    return hover;
                }
                startY += lineHeight + lineSpacing;
            }
            return null;
        }

        private @Nullable HoverEvent findHoverEventInLine(
                @NotNull Font font,
                @NotNull FormattedCharSequence line,
                float lineX,
                float lineWidth,
                float scale,
                float mouseX
        ) {
            float relativeX = mouseX - lineX;
            if (relativeX < 0.0F || relativeX > lineWidth) {
                return null;
            }
            float[] cursor = new float[]{0.0F};
            HoverEvent[] found = new HoverEvent[1];
            line.accept((index, style, codePoint) -> {
                if (found[0] != null) {
                    return false;
                }
                float charWidth = font.width(new String(Character.toChars(codePoint))) * scale;
                float next = cursor[0] + charWidth;
                if (relativeX >= cursor[0] && relativeX <= next) {
                    if (isReplySuggestionStyle(style)) {
                        return false;
                    }
                    HoverEvent hover = style.getHoverEvent();
                    if (hover != null) {
                        found[0] = hover;
                        return false;
                    }
                    return false;
                }
                cursor[0] = next;
                return true;
            });
            return found[0];
        }

        private @Nullable HoverTooltips buildHoverTooltips(@NotNull HoverEvent hover) {
            if (hover.getAction() == HoverEvent.Action.SHOW_ITEM) {
                HoverEvent.ItemStackInfo info = hover.getValue(HoverEvent.Action.SHOW_ITEM);
                ItemStack stack = info != null ? info.getItemStack() : ItemStack.EMPTY;
                if (stack.isEmpty()) {
                    stack = new ItemStack(Blocks.BARRIER);
                }
                return new HoverTooltips(
                        DrawerHelper.getItemToolTip(stack),
                        stack.getTooltipImage().orElse(null),
                        null,
                        stack
                );
            }
            if (hover.getAction() == HoverEvent.Action.SHOW_TEXT) {
                Component hoverText = hover.getValue(HoverEvent.Action.SHOW_TEXT);
                if (hoverText == null) {
                    return null;
                }
                Component sanitized = stripSpotAddLine(hoverText);
                if (sanitized == null) {
                    return null;
                }
                return new HoverTooltips(List.of(sanitized), null, null, ItemStack.EMPTY);
            }
            return null;
        }

        private boolean isReplySuggestionStyle(@NotNull Style style) {
            ClickEvent clickEvent = style.getClickEvent();
            return clickEvent != null && clickEvent.getAction() == ClickEvent.Action.SUGGEST_COMMAND;
        }

        private @Nullable Component stripSpotAddLine(@NotNull Component hoverText) {
            if (isSpotAddComponent(hoverText)) {
                return null;
            }
            if (hoverText.getSiblings().isEmpty()) {
                return hoverText;
            }
            MutableComponent sanitized = hoverText.copy();
            List<Component> siblings = sanitized.getSiblings();
            for (int i = 0; i < siblings.size(); i++) {
                Component part = siblings.get(i);
                if (!isSpotAddComponent(part)) {
                    continue;
                }
                if (i > 0 && isNewLineComponent(siblings.get(i - 1))) {
                    siblings.remove(i - 1);
                    i--;
                }
                siblings.remove(i);
                i--;
            }
            return sanitized;
        }

        private boolean isSpotAddComponent(@NotNull Component component) {
            return Component.translatable(SPOT_FTB_ADD_TOOLTIP_KEY)
                    .getString()
                    .equals(component.getString());
        }

        private boolean isNewLineComponent(@NotNull Component component) {
            return "\n".equals(component.getString());
        }

        private @Nullable ItemIconPlacement findItemIconPlacement() {
            Component text = getText();
            if (text == null) {
                return null;
            }
            float width = getContentWidth();
            if (width <= 0.0F) {
                return null;
            }

            Font font = getFont();
            float lineHeight = getTextStyle().fontSize();
            float lineSpacing = getTextStyle().lineSpacing();
            float scale = lineHeight / font.lineHeight;

            List<Tuple<FormattedCharSequence, Float>> lines =
                    TextUtilities.computeFormattedLines(font, text, lineHeight, width);
            if (lines.isEmpty()) {
                return null;
            }

            List<Tuple<FormattedCharSequence, Float>> displayLines = lines;
            TextWrap textWrap = getTextStyle().textWrap();
            if (textWrap == TextWrap.HIDE) {
                displayLines = lines.subList(0, Math.min(1, lines.size()));
            }

            float totalTextHeight = displayLines.size() * (lineHeight + lineSpacing) - lineSpacing;
            float startY = getContentY();
            switch (getTextStyle().textAlignVertical()) {
                case TOP -> startY = getContentY();
                case CENTER -> startY = getContentY() + (getContentHeight() - totalTextHeight) / 2;
                case BOTTOM -> startY = getContentY() + (getContentHeight() - totalTextHeight);
            }

            boolean roll = textWrap == TextWrap.ROLL || (textWrap == TextWrap.HOVER_ROLL && isSelfOrChildHover());
            for (int i = 0; i < displayLines.size(); i++) {
                Tuple<FormattedCharSequence, Float> tuple = displayLines.get(i);
                FormattedCharSequence line = tuple.getA();
                float lineWidth = tuple.getB();
                float lineX = getContentX();

                if (roll && lineWidth > width) {
                    float rollSpeed = getTextStyle().rollSpeed();
                    float totalW = width + lineWidth + 10;
                    float t = rollSpeed > 0
                            ? (((rollSpeed * Math.abs((int) (System.currentTimeMillis() % 1000000)) / 10)
                            % (totalW)) / (totalW))
                            : 0.5F;
                    lineX = getContentX() + width - totalW * t;
                } else {
                    switch (getTextStyle().textAlignHorizontal()) {
                        case LEFT -> lineX = getContentX();
                        case CENTER -> lineX = (lineWidth > width) ? getContentX()
                                : (getContentX() + (width - lineWidth) / 2);
                        case RIGHT -> lineX = getContentX() + (width - lineWidth);
                    }
                }

                float lineY = startY + i * (lineHeight + lineSpacing);
                ItemIconPlacement placement = findItemIconInLine(font, line, lineX, lineY, scale);
                if (placement != null) {
                    return placement;
                }
            }
            return null;
        }

        private ItemIconPlacement findItemIconInLine(
                @NotNull Font font,
                @NotNull FormattedCharSequence line,
                float lineX,
                float lineY,
                float scale
        ) {
            StringBuilder before = new StringBuilder();
            ItemIconPlacement[] placement = new ItemIconPlacement[1];
            float halfSpace = font.width(ITEM_ICON_PLACEHOLDER) / 2.0F * scale;

            line.accept((index, style, codePoint) -> {
                if (placement[0] != null) {
                    return false;
                }
                if (codePoint != ' ' && endsWithPlaceholder(before)) {
                    HoverEvent hover = style.getHoverEvent();
                    if (hover != null && hover.getAction() == HoverEvent.Action.SHOW_ITEM) {
                        HoverEvent.ItemStackInfo info = hover.getValue(HoverEvent.Action.SHOW_ITEM);
                        ItemStack stack = info != null ? info.getItemStack() : ItemStack.EMPTY;
                        if (stack.isEmpty()) {
                            stack = new ItemStack(Blocks.BARRIER);
                        }
                        String beforeText = before.substring(0, before.length() - ITEM_ICON_PLACEHOLDER.length());
                        float shift = font.width(beforeText) * scale - halfSpace + ITEM_ICON_EXTRA_SHIFT * scale;
                        placement[0] = new ItemIconPlacement(stack, lineX + shift, lineY - 1.0F);
                        return false;
                    }
                }
                before.appendCodePoint(codePoint);
                return true;
            });

            return placement[0];
        }

        private boolean endsWithPlaceholder(@NotNull StringBuilder builder) {
            int length = builder.length();
            if (length < ITEM_ICON_PLACEHOLDER.length()) {
                return false;
            }
            for (int i = 0; i < ITEM_ICON_PLACEHOLDER.length(); i++) {
                if (builder.charAt(length - ITEM_ICON_PLACEHOLDER.length() + i) != ' ') {
                    return false;
                }
            }
            return true;
        }
    }

    private record ItemIconPlacement(ItemStack stack, float x, float y) {
    }
}
