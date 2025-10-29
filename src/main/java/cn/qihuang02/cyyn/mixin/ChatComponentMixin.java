package cn.qihuang02.cyyn.mixin;

import cn.qihuang02.cyyn.common.config.Config;
import cn.qihuang02.cyyn.util.CyynItemHoverArea;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.*;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin {
    @Unique
    private static final float cyyn$ITEM_ICON_SCALE = 0.6F;

    @Unique
    private static final float cyyn$ITEM_ICON_LEADING_PADDING = 2.0F;

    @Unique
    private static final float cyyn$ITEM_ICON_TEXT_PADDING = 3.0F;

    @Unique
    private static final float cyyn$ITEM_VERTICAL_OFFSET = 1.0F;

    @Unique
    private final List<CyynItemHoverArea> cyyn$itemHoverAreas = new ArrayList<>();

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;III)I"))
    private int cyyn$renderItemMentions(GuiGraphics guiGraphics, Font font, FormattedCharSequence text, int x, int y, int color) {
        if (!Config.renderItemTextures || !cyyn$containsItemHover(text)) {
            return guiGraphics.drawString(font, text, x, y, color);
        }

        int result = guiGraphics.drawString(font, text, x, y, color);
        cyyn$renderItemIcons(guiGraphics, font, text, x, y, color);
        return result;
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void cyyn$prepareItemHoverAreas(GuiGraphics pGuiGraphics, int pTickCount, int pMouseX, int pMouseY, CallbackInfo ci) {
        this.cyyn$itemHoverAreas.clear();
    }

    @Unique
    private void cyyn$renderItemIcons(GuiGraphics guiGraphics, Font font, FormattedCharSequence text, int baseX, int baseY, int color) {
        if (!Config.renderItemTextures) {
            return;
        }

        float alpha = (float) ((color >> 24) & 0xFF) / 255.0F;
        if (alpha <= 0.0F) {
            return;
        }

        float iconWidth = 16.0F * cyyn$ITEM_ICON_SCALE;
        float[] advance = new float[]{0.0F};
        float[] trailingSpaceWidth = new float[]{0.0F};
        int[] trailingSpaceCount = new int[]{0};
        text.accept((index, style, codePoint) -> {
            Style effectiveStyle = style == null ? Style.EMPTY : style;
            HoverEvent.ItemStackInfo itemInfo = cyyn$getItemHover(effectiveStyle);
            if (codePoint == ' ') {
                trailingSpaceCount[0]++;
                trailingSpaceWidth[0] += cyyn$getGlyphWidth(font, effectiveStyle, codePoint);
            } else {
                trailingSpaceCount[0] = 0;
                trailingSpaceWidth[0] = 0.0F;
            }

            if (itemInfo != null && codePoint == '[') {
                ItemStack stack = itemInfo.getItemStack().copy();
                if (!stack.isEmpty()) {
                    float iconLeft = cyyn$computeIconLeft(baseX, advance[0], trailingSpaceWidth[0], iconWidth);
                    cyyn$drawItemIcon(guiGraphics, stack, iconLeft, baseY, alpha);
                    cyyn$recordItemHoverArea(effectiveStyle, iconLeft, baseY, iconWidth);
                }
                trailingSpaceCount[0] = 0;
                trailingSpaceWidth[0] = 0.0F;
            }
            advance[0] += cyyn$getGlyphWidth(font, effectiveStyle, codePoint);
            return true;
        });
    }

    @Unique
    private float cyyn$computeIconLeft(int baseX, float advanceWithoutPadding, float trailingSpaceWidth, float iconWidth) {
        float desiredWidth = cyyn$ITEM_ICON_LEADING_PADDING + iconWidth + cyyn$ITEM_ICON_TEXT_PADDING;
        float available = Math.max(trailingSpaceWidth, desiredWidth);
        float spacesStart = baseX + advanceWithoutPadding - trailingSpaceWidth;
        float centeredOffset = Math.max(0.0F, available - iconWidth) * 0.5F;
        float baseline = trailingSpaceWidth > 0.0F ? spacesStart : baseX + advanceWithoutPadding - desiredWidth;
        return baseline + cyyn$ITEM_ICON_LEADING_PADDING + centeredOffset;
    }

    @Unique
    private float cyyn$getGlyphWidth(@NotNull Font font, Style style, int codePoint) {
        String glyph = new String(Character.toChars(codePoint));
        FormattedText formatted = FormattedText.of(glyph, style);
        return font.getSplitter().stringWidth(formatted);
    }

    @Unique
    private void cyyn$drawItemIcon(@NotNull GuiGraphics guiGraphics, ItemStack stack, float iconX, int textY, float alpha) {
        float iconY = textY - cyyn$ITEM_VERTICAL_OFFSET;

        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(iconX, iconY, 0.0F);
        poseStack.scale(cyyn$ITEM_ICON_SCALE, cyyn$ITEM_ICON_SCALE, 1.0F);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        guiGraphics.renderItem(stack, 0, 0);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();

        poseStack.popPose();
    }

    @Unique
    private void cyyn$recordItemHoverArea(Style style, float iconLeft, int baseY, float iconSize) {
        if (style == null) {
            return;
        }

        float iconTop = baseY - cyyn$ITEM_VERTICAL_OFFSET;
        float iconRight = iconLeft + iconSize;

        float iconBottom = iconTop + iconSize;
        cyyn$registerHoverArea(iconLeft, iconTop, iconRight, iconBottom, style);
    }

    @Unique
    private boolean cyyn$containsItemHover(@NotNull FormattedCharSequence text) {
        final boolean[] found = new boolean[]{false};
        text.accept((index, style, codePoint) -> {
            if (cyyn$shouldRenderItem(style, codePoint)) {
                found[0] = true;
                return false;
            }
            return true;
        });
        return found[0];
    }

    @Unique
    private boolean cyyn$shouldRenderItem(Style style, int codePoint) {
        if (codePoint != '[' || style == null) {
            return false;
        }
        return cyyn$getItemHover(style) != null;
    }

    @Unique
    private HoverEvent.@Nullable ItemStackInfo cyyn$getItemHover(@NotNull Style style) {
        HoverEvent hoverEvent = style.getHoverEvent();
        return hoverEvent != null ? hoverEvent.getValue(HoverEvent.Action.SHOW_ITEM) : null;
    }

    @Unique
    private void cyyn$registerHoverArea(float left, float top, float right, float bottom, Style style) {
        int minX = Mth.floor(Math.min(left, right));
        int minY = Mth.floor(Math.min(top, bottom));
        int maxX = Mth.ceil(Math.max(left, right));
        int maxY = Mth.ceil(Math.max(top, bottom));
        if (maxX <= minX || maxY <= minY) {
            return;
        }

        this.cyyn$itemHoverAreas.add(new CyynItemHoverArea(minX, minY, maxX - minX, maxY - minY, style));
    }

    @Inject(method = "getClickedComponentStyleAt", at = @At("HEAD"), cancellable = true)
    private void cyyn$expandItemHoverArea(double mouseX, double mouseY, CallbackInfoReturnable<Style> cir) {
        if (!Config.renderItemTextures) {
            return;
        }

        int x = Mth.floor(mouseX);
        int y = Mth.floor(mouseY);
        for (CyynItemHoverArea area : this.cyyn$itemHoverAreas) {
            if (area.contains(x, y)) {
                cir.setReturnValue(area.style());
                cir.cancel();
                return;
            }
        }
    }
}
