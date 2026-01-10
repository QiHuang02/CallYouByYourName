package cn.qihuang02.callyou.client.screen;

import cn.qihuang02.callyou.core.saveddata.MentionRecord;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.style.LayoutStyle;
import com.lowdragmc.lowdraglib2.gui.ui.styletemplate.Sprites;
import net.minecraft.client.Minecraft;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import org.appliedenergistics.yoga.YogaAlign;
import org.appliedenergistics.yoga.YogaFlexDirection;
import org.appliedenergistics.yoga.YogaJustify;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class MentionLogRow {
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("MM-dd HH:mm").withZone(ZoneId.systemDefault());
    private static final Component DELETE_LABEL = Component.translatable("screen.callyou.history.delete");
    private static final Component COORDS_LABEL = Component.translatable("screen.callyou.history.coords");
    private static final String COPIED_KEY = "screen.callyou.history.coords_copied";

    private final UIElement element;

    public MentionLogRow(@NotNull MentionRecord record, @NotNull Runnable onDelete) {
        String headerText = "[" + DATE_FORMATTER.format(Instant.ofEpochMilli(record.timestamp())) + "] "
                + record.senderName();

        Label header = new Label();
        header.setText(Component.literal(headerText));
        header.layout(LayoutStyle::widthStretch);
        header.textStyle(style -> style.textColor(record.read() ? 0xDDDDDD : 0xFFE2A0));

        Label body = new Label();
        body.setText(Component.literal(buildPreview(record.message().getString())));
        body.layout(LayoutStyle::widthStretch);
        body.textStyle(style -> style.textColor(record.read() ? 0xCCCCCC : 0xFFFFFF));

        UIElement actionRow = new UIElement()
                .layout(style -> style.flexDirection(YogaFlexDirection.ROW)
                        .gapColumn(4)
                        .widthStretch()
                        .alignItems(YogaAlign.CENTER)
                        .justifyItems(YogaJustify.FLEX_END));

        GlobalPos location = record.location();
        if (location != null) {
            Button coordsButton = new Button();
            coordsButton.setText(COORDS_LABEL);
            coordsButton.layout(style -> style.width(70).height(14));
            MentionUIStyles.applyButtonStyle(coordsButton);
            coordsButton.setOnClick(event -> copyLocation(location));
            actionRow.addChildren(coordsButton);
        }

        Button deleteButton = new Button();
        deleteButton.setText(DELETE_LABEL);
        deleteButton.layout(style -> style.width(60).height(14));
        MentionUIStyles.applyButtonStyle(deleteButton);
        deleteButton.setOnClick(event -> onDelete.run());
        actionRow.addChildren(deleteButton);

        this.element = new UIElement()
                .layout(style -> style.flexDirection(YogaFlexDirection.COLUMN)
                        .gapRow(2)
                        .widthStretch()
                        .paddingAll(4))
                .style(style -> style.background(record.read() ? Sprites.RECT_RD : Sprites.RECT_RD_LIGHT))
                .addChildren(header, body, actionRow);
        this.element.style(style -> style.tooltips(buildTooltip(record)));
    }

    public UIElement getElement() {
        return element;
    }

    private void copyLocation(@NotNull GlobalPos pos) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.player == null) {
            return;
        }
        String value = formatLocation(pos);
        minecraft.keyboardHandler.setClipboard(value);
        minecraft.player.sendSystemMessage(Component.translatable(COPIED_KEY, value));
    }

    private @NotNull String formatLocation(@NotNull GlobalPos pos) {
        return pos.dimension().location() + " " +
                pos.pos().getX() + ", " +
                pos.pos().getY() + ", " +
                pos.pos().getZ();
    }

    private @NotNull String buildPreview(@NotNull String raw) {
        String condensed = raw.replaceAll("\\s+", " ").trim();
        if (condensed.length() > 80) {
            return condensed.substring(0, 80) + "...";
        }
        return condensed;
    }

    private Component buildTooltip(@NotNull MentionRecord record) {
        Component tooltip = Component.literal(record.message().getString());
        GlobalPos location = record.location();
        if (location != null) {
            tooltip = tooltip.copy().append(Component.literal("\n" + formatLocation(location)));
        }
        return tooltip;
    }
}
