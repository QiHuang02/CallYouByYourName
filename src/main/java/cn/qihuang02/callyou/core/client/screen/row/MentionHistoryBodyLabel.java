package cn.qihuang02.callyou.core.client.screen.row;

import cn.qihuang02.callyou.config.CallYouConfig;
import cn.qihuang02.callyou.core.client.render.ItemIconRenderUtil;
import cn.qihuang02.callyou.core.client.screen.MentionUIStyles;
import cn.qihuang02.callyou.util.ComponentTraversal;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.event.HoverTooltips;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib2.utils.TextUtilities;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.*;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import org.appliedenergistics.yoga.YogaPositionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

final class MentionHistoryBodyLabel extends Label {
    private static final float ITEM_ICON_EXTRA_SHIFT = 1.0F;
    private static final String SPOT_FTB_ADD_TOOLTIP_KEY = "message.callyou.spot.ftb.add";

    private ItemSlot inlineItemSlot;
    private ItemSlot hoverItemSlot;

    MentionHistoryBodyLabel() {
        addEventListener(UIEvents.HOVER_TOOLTIPS, this::onHoverTooltips);
        refreshInlineItemSlot();
    }

    @Override
    public @NotNull MentionHistoryBodyLabel setText(@NotNull Component text) {
        super.setText(text);
        refreshInlineItemSlot();
        return this;
    }

    @Override
    protected void onLayoutChanged() {
        super.onLayoutChanged();
        refreshInlineItemSlot();
    }

    private void ensureInlineItemSlot() {
        if (inlineItemSlot != null) {
            return;
        }
        inlineItemSlot = MentionUIStyles.createItemSlot(ItemStack.EMPTY, 18, false, true);
        inlineItemSlot.layout(style -> style.positionType(YogaPositionType.ABSOLUTE).paddingAll(0));
        inlineItemSlot.setVisible(false);
        addChild(inlineItemSlot);
    }

    private void ensureHoverItemSlot() {
        if (hoverItemSlot != null) {
            return;
        }
        hoverItemSlot = MentionUIStyles.createItemSlot(ItemStack.EMPTY, 18, false, false);
        hoverItemSlot.layout(style -> style.positionType(YogaPositionType.ABSOLUTE).paddingAll(0));
        hoverItemSlot.setVisible(false);
        addChild(hoverItemSlot);
    }

    private void refreshInlineItemSlot() {
        if (CallYouConfig.CLIENT.itemIconRenderMode.get() != CallYouConfig.ItemIconRenderMode.INLINE) {
            if (inlineItemSlot != null) {
                inlineItemSlot.setVisible(false);
            }
            return;
        }
        ensureInlineItemSlot();
        ItemIconPlacement placement = findItemIconPlacement();
        if (placement == null) {
            inlineItemSlot.setVisible(false);
            return;
        }
        float itemSize = Math.max(9.0F, getTextStyle().fontSize());
        float localX = placement.x() - getContentX();
        float localY = placement.y() - getContentY();
        inlineItemSlot.setItem(placement.stack());
        inlineItemSlot.layout(style -> style.positionType(YogaPositionType.ABSOLUTE)
                .left(localX)
                .top(localY)
                .width(itemSize)
                .height(itemSize)
                .paddingAll(0));
        inlineItemSlot.setVisible(true);
    }

    private void onHoverTooltips(UIEvent event) {
        HoverEvent hover = findHoverEventAtMouse();
        updateHoverItemSlot(hover);
        if (hover == null) {
            return;
        }
        HoverTooltips tooltips = buildHoverTooltips(hover);
        if (tooltips == null || tooltips.tooltipTexts().isEmpty()) {
            return;
        }
        event.hoverTooltips = tooltips;
    }

    private void updateHoverItemSlot(@Nullable HoverEvent hover) {
        if (CallYouConfig.CLIENT.itemIconRenderMode.get() != CallYouConfig.ItemIconRenderMode.HOVER) {
            if (hoverItemSlot != null) {
                hoverItemSlot.setVisible(false);
            }
            return;
        }
        if (hover == null || hover.getAction() != HoverEvent.Action.SHOW_ITEM) {
            if (hoverItemSlot != null) {
                hoverItemSlot.setVisible(false);
            }
            return;
        }
        HoverEvent.ItemStackInfo info = hover.getValue(HoverEvent.Action.SHOW_ITEM);
        ItemStack stack = info != null ? info.getItemStack() : ItemStack.EMPTY;
        if (stack.isEmpty()) {
            stack = new ItemStack(Blocks.BARRIER);
        }
        var ui = getModularUI();
        if (ui == null) {
            if (hoverItemSlot != null) {
                hoverItemSlot.setVisible(false);
            }
            return;
        }
        ensureHoverItemSlot();
        float itemSize = Math.max(9.0F, getTextStyle().fontSize());
        float rawX = ui.getLastMouseX() - getContentX() - itemSize - 2.0F;
        float rawY = ui.getLastMouseY() - getContentY() - itemSize / 2.0F;
        float localX = Math.max(0.0F, rawX);
        float localY = Math.max(0.0F, rawY);
        hoverItemSlot.setItem(stack);
        hoverItemSlot.layout(style -> style.positionType(YogaPositionType.ABSOLUTE)
                .left(localX)
                .top(localY)
                .width(itemSize)
                .height(itemSize)
                .paddingAll(0));
        hoverItemSlot.setVisible(true);
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
        if (relativeX < 0.0F) {
            return null;
        }
        float[] cursor = new float[]{0.0F};
        HoverEvent[] found = new HoverEvent[1];
        boolean[] inItem = new boolean[]{false};
        float iconWidth = Math.max(9.0F, getTextStyle().fontSize());
        line.accept((index, style, codePoint) -> {
            if (found[0] != null) {
                return false;
            }
            boolean isItem = ItemIconRenderUtil.resolveItemStack(style) != null;
            if (isItem && !inItem[0]) {
                float gapEnd = cursor[0] + iconWidth;
                if (relativeX >= cursor[0] && relativeX <= gapEnd) {
                    if (!isReplySuggestionStyle(style)) {
                        HoverEvent hover = style.getHoverEvent();
                        if (hover != null) {
                            found[0] = hover;
                            return false;
                        }
                    }
                }
                cursor[0] = gapEnd;
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
            inItem[0] = isItem;
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
        Component spotAdd = ComponentTraversal.findFirst(
                hoverText,
                component -> isSpotAddComponent(component) ? component : null
        );
        if (spotAdd == null) {
            return hoverText;
        }
        if (spotAdd == hoverText) {
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

        line.accept((index, style, codePoint) -> {
            if (placement[0] != null) {
                return false;
            }
            ItemStack stack = ItemIconRenderUtil.resolveItemStack(style);
            if (stack != null) {
                float beforeWidth = font.width(before.toString()) * scale;
                float shift = beforeWidth + ITEM_ICON_EXTRA_SHIFT * scale;
                placement[0] = new ItemIconPlacement(stack, lineX + shift, lineY - 1.0F);
                return false;
            }
            before.appendCodePoint(codePoint);
            return true;
        });

        return placement[0];
    }

    private record ItemIconPlacement(ItemStack stack, float x, float y) {
    }
}
