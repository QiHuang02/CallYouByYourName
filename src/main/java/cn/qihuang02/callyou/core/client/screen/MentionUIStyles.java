package cn.qihuang02.callyou.core.client.screen;

import com.lowdragmc.lowdraglib2.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.elements.*;
import com.lowdragmc.lowdraglib2.gui.ui.styletemplate.Sprites;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class MentionUIStyles {
    public static void applyScrollerStyle(@NotNull ScrollerView view) {
        view.verticalScroller.scrollContainer.style(style -> style.background(Sprites.SCROLL_CONTAINER_V));
        view.verticalScroller.scrollBar.buttonStyle(style -> style
                .baseTexture(Sprites.SCROLL_BAR_V)
                .hoverTexture(Sprites.SCROLL_BAR_LIGHT_V)
                .pressedTexture(Sprites.SCROLL_BAR_WHITE_V));
        view.horizontalScroller.scrollContainer.style(style -> style.background(Sprites.SCROLL_CONTAINER_H));
        view.horizontalScroller.scrollBar.buttonStyle(style -> style
                .baseTexture(Sprites.SCROLL_BAR_H)
                .hoverTexture(Sprites.SCROLL_BAR_LIGHT_H)
                .pressedTexture(Sprites.SCROLL_BAR_WHITE_H));
    }

    public static void applySwitchStyle(@NotNull Switch toggle) {
        toggle.switchStyle(style -> style
                .baseTexture(Sprites.RECT_RD_DARK)
                .pressedTexture(Sprites.RECT_RD_T_DARK)
                .unmarkTexture(Sprites.RECT_RD)
                .markTexture(Sprites.RECT_RD_LIGHT));
    }

    public static void applyButtonStyle(@NotNull Button button) {
        button.buttonStyle(style -> style
                .baseTexture(Sprites.RECT_RD)
                .hoverTexture(Sprites.RECT_RD_LIGHT)
                .pressedTexture(Sprites.RECT_RD_T_DARK));
    }

    public static void applyTextFieldStyle(@NotNull TextField field) {
        field.style(style -> style.background(Sprites.RECT_RD));
        field.textFieldStyle(style -> style.focusOverlay(Sprites.RECT_RD_T_LIGHT));
    }

    public static @NotNull ItemSlot createItemSlot(
            @NotNull ItemStack item,
            int size,
            boolean isRenderBackgroundTexture,
            boolean showItemTooltips
    ) {
        return (ItemSlot) new ItemSlot()
                .setItem(item)
                .slotStyle(slotStyle -> {
                    if (isRenderBackgroundTexture) {
                        slotStyle.hoverOverlay(new ColorRectTexture(0x80FFFFFF));
                    } else {
                        slotStyle.hoverOverlay(IGuiTexture.EMPTY);
                    }
                    slotStyle.showItemTooltips(showItemTooltips);
                })
                .layout(layout -> {
                    layout.setWidth(size);
                    layout.setHeight(size);
                })
                .style(style -> {
                    if (!isRenderBackgroundTexture) {
                        style.backgroundTexture(IGuiTexture.EMPTY);
                    }
                });
    }
}
