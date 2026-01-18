package cn.qihuang02.callyou.core.client.render;

import cn.qihuang02.callyou.config.CallYouConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ItemIconRenderUtil {
    public static final float ITEM_ICON_SCALE = 0.6F;
    public static final float ITEM_ICON_EXTRA_SHIFT = 1.0F;

    @OnlyIn(Dist.CLIENT)
    public static @NotNull FormattedCharSequence padItemIconText(
            @NotNull FormattedCharSequence line,
            @NotNull Font font
    ) {
        if (CallYouConfig.CLIENT.itemIconRenderMode.get() != CallYouConfig.ItemIconRenderMode.INLINE) {
            return line;
        }
        int iconWidth = Math.round(16.0F * ITEM_ICON_SCALE + ITEM_ICON_EXTRA_SHIFT);
        int paddingCount = Math.max(1, (int) Math.floor(iconWidth / (float) Math.max(1, font.width(" "))));
        paddingCount += 1;
        String padding = " ".repeat(paddingCount);

        return sink -> {
            boolean[] inItem = new boolean[]{false};
            return line.accept((index, style, codePoint) -> {
                boolean isItem = isItemIconStyle(style);
                if (isItem && !inItem[0]) {
                    for (int i = 0; i < padding.length(); i++) {
                        if (!sink.accept(index, style, padding.charAt(i))) {
                            return false;
                        }
                    }
                }
                if (!sink.accept(index, style, codePoint)) {
                    return false;
                }
                inItem[0] = isItem;
                return true;
            });
        };
    }

    @OnlyIn(Dist.CLIENT)
    public static void renderItemIconsInLine(
            GuiGraphics guiGraphics,
            Font font,
            FormattedCharSequence line,
            int baseX,
            int baseY,
            int color
    ) {
        if (CallYouConfig.CLIENT.itemIconRenderMode.get() != CallYouConfig.ItemIconRenderMode.INLINE) {
            return;
        }

        if (Minecraft.getInstance() == null) {
            return;
        }

        StringBuilder before = new StringBuilder();

        line.accept((index, style, codePoint) -> {
            HoverEvent hover = style.getHoverEvent();
            boolean isItem = hover != null && hover.getAction() == HoverEvent.Action.SHOW_ITEM;

            if (isItem) {
                if (renderSingleItemIcon(guiGraphics, font, before.toString(),
                        0.0F, baseX, baseY, style, color)) {
                    return false;
                }
            }

            before.appendCodePoint(codePoint);
            return true;
        });
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean renderSingleItemIcon(
            GuiGraphics guiGraphics,
            Font font,
            String beforeText,
            float extraShift,
            int baseX,
            int baseY,
            @NotNull Style style,
            int color
    ) {
        ItemStack stack = resolveItemStack(style);
        if (stack == null) {
            return false;
        }

        float alpha = (color >> 24 & 0xFF) / 255.0F;
        if (alpha <= 0.0F) {
            return false;
        }

        float shift = font.width(beforeText) + extraShift + ITEM_ICON_EXTRA_SHIFT;

        PoseStack pose = guiGraphics.pose();
        pose.pushPose();

        pose.translate(baseX + shift, baseY - 1, 200.0F);
        pose.scale(ITEM_ICON_SCALE, ITEM_ICON_SCALE, 1.0F);

        guiGraphics.renderItem(stack, 0, 0);

        pose.popPose();
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    public static void renderHoverItemIcon(
            @NotNull GuiGraphics graphics,
            @Nullable Minecraft minecraft,
            int mouseX,
            int mouseY
    ) {
        if (CallYouConfig.CLIENT.itemIconRenderMode.get() != CallYouConfig.ItemIconRenderMode.HOVER) {
            return;
        }
        if (minecraft == null) {
            return;
        }
        Style style = minecraft.gui.getChat().getClickedComponentStyleAt(mouseX, mouseY);
        if (style == null) {
            return;
        }
        HoverEvent hover = style.getHoverEvent();
        if (hover == null || hover.getAction() != HoverEvent.Action.SHOW_ITEM) {
            return;
        }
        HoverEvent.ItemStackInfo info = hover.getValue(HoverEvent.Action.SHOW_ITEM);
        ItemStack stack = info != null ? info.getItemStack() : ItemStack.EMPTY;
        if (stack.isEmpty()) {
            stack = new ItemStack(Blocks.BARRIER);
        }
        int iconX = Math.max(0, mouseX - 18);
        int iconY = Math.max(0, mouseY - 8);
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 400.0F);
        graphics.renderItem(stack, iconX, iconY);
        graphics.pose().popPose();
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean isItemIconStyle(@NotNull Style style) {
        return resolveItemStack(style) != null;
    }

    @OnlyIn(Dist.CLIENT)
    public static @Nullable ItemStack resolveItemStack(@NotNull Style style) {
        HoverEvent hover = style.getHoverEvent();
        if (hover == null || hover.getAction() != HoverEvent.Action.SHOW_ITEM) {
            return null;
        }
        HoverEvent.ItemStackInfo info = hover.getValue(HoverEvent.Action.SHOW_ITEM);
        ItemStack stack = info != null ? info.getItemStack() : ItemStack.EMPTY;
        if (stack.isEmpty()) {
            stack = new ItemStack(Blocks.BARRIER);
        }
        String insertion = style.getInsertion();
        if (insertion == null || !insertion.equals(stack.getDescriptionId())) {
            return null;
        }
        return stack;
    }
}
