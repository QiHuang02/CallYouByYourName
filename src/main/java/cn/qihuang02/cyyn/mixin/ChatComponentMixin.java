package cn.qihuang02.cyyn.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin {
    @Unique
    private static final float cyyn$ITEM_ICON_SCALE = 0.6F;

    @Unique
    private static final float cyyn$ITEM_ICON_LEADING_PADDING = 2.0F;

    @Unique
    private static final float cyyn$ITEM_ICON_TEXT_PADDING = 3.0F;

    @Unique
    private static final float cyyn$ITEM_VERTICAL_OFFSET = 2.0F;

    @Unique
    private static int cyyn$itemPaddingSpaceCount = -1;

    @Unique
    private static float cyyn$itemPaddingWidth = 0.0F;

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;III)I"))
    private int cyyn$renderItemMentions(GuiGraphics guiGraphics, Font font, FormattedCharSequence text, int x, int y, int color) {
        if (!cyyn$containsItemHover(text)) {
            return guiGraphics.drawString(font, text, x, y, color);
        }

        FormattedCharSequence paddedText = cyyn$withItemPadding(text, font);
        int result = guiGraphics.drawString(font, paddedText, x, y, color);
        cyyn$renderItemIcons(guiGraphics, font, text, x, y, color);
        return result;
    }

    @Unique
    private void cyyn$renderItemIcons(GuiGraphics guiGraphics, Font font, FormattedCharSequence text, int baseX, int baseY, int color) {
        float alpha = (float) ((color >> 24) & 0xFF) / 255.0F;
        if (alpha <= 0.0F) {
            return;
        }

        cyyn$ensurePaddingMetrics(font);

        float[] advance = new float[]{0.0F};
        text.accept((index, style, codePoint) -> {
            Style effectiveStyle = style == null ? Style.EMPTY : style;
            HoverEvent.ItemStackInfo itemInfo = cyyn$getItemHover(effectiveStyle);
            if (itemInfo != null && codePoint == '[') {
                ItemStack stack = itemInfo.getItemStack().copy();
                if (!stack.isEmpty()) {
                    float iconLeft = cyyn$computeIconLeft(baseX, advance[0]);
                    cyyn$drawItemIcon(guiGraphics, stack, iconLeft, baseY, alpha);
                }
                advance[0] += cyyn$itemPaddingWidth;
            }
            advance[0] += cyyn$getGlyphWidth(font, effectiveStyle, codePoint);
            return true;
        });
    }

    @Unique
    private float cyyn$computeIconLeft(int baseX, float advanceWithoutPadding) {
        float iconWidth = 16.0F * cyyn$ITEM_ICON_SCALE;
        float desiredSpacing = cyyn$ITEM_ICON_LEADING_PADDING + iconWidth + cyyn$ITEM_ICON_TEXT_PADDING;
        float extraPadding = Math.max(0.0F, cyyn$itemPaddingWidth - desiredSpacing);
        return baseX + advanceWithoutPadding + cyyn$ITEM_ICON_LEADING_PADDING + (extraPadding * 0.5F);
    }

    @Unique
    private float cyyn$getGlyphWidth(Font font, Style style, int codePoint) {
        String glyph = new String(Character.toChars(codePoint));
        FormattedText formatted = FormattedText.of(glyph, style);
        return font.getSplitter().stringWidth(formatted);
    }

    @Unique
    private void cyyn$drawItemIcon(GuiGraphics guiGraphics, ItemStack stack, float iconLeft, int textY, float alpha) {
        float scaledSize = 16.0F * cyyn$ITEM_ICON_SCALE;
        float iconX = iconLeft;
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
    private boolean cyyn$containsItemHover(FormattedCharSequence text) {
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
    private FormattedCharSequence cyyn$withItemPadding(FormattedCharSequence text, Font font) {
        cyyn$ensurePaddingMetrics(font);
        if (cyyn$itemPaddingSpaceCount <= 0) {
            return text;
        }

        return visitor -> {
            final int[] extraIndex = new int[]{0};
            return text.accept((index, style, codePoint) -> {
                Style effectiveStyle = style == null ? Style.EMPTY : style;
                int adjustedIndex = index + extraIndex[0];
                if (cyyn$shouldRenderItem(style, codePoint)) {
                    for (int i = 0; i < cyyn$itemPaddingSpaceCount; i++) {
                        if (!visitor.accept(adjustedIndex++, effectiveStyle, ' ')) {
                            return false;
                        }
                    }
                    extraIndex[0] += cyyn$itemPaddingSpaceCount;
                }
                return visitor.accept(adjustedIndex, effectiveStyle, codePoint);
            });
        };
    }

    @Unique
    private boolean cyyn$shouldRenderItem(Style style, int codePoint) {
        if (codePoint != '[' || style == null) {
            return false;
        }
        return cyyn$getItemHover(style) != null;
    }

    @Unique
    private HoverEvent.ItemStackInfo cyyn$getItemHover(Style style) {
        HoverEvent hoverEvent = style.getHoverEvent();
        return hoverEvent != null ? hoverEvent.getValue(HoverEvent.Action.SHOW_ITEM) : null;
    }

    @Unique
    private void cyyn$ensurePaddingMetrics(Font font) {
        if (cyyn$itemPaddingSpaceCount >= 0) {
            return;
        }

        int spaceWidth = font.width(" ");
        if (spaceWidth <= 0) {
            cyyn$itemPaddingSpaceCount = 0;
            cyyn$itemPaddingWidth = 0.0F;
            return;
        }

        float desiredSpacing = cyyn$ITEM_ICON_LEADING_PADDING + (16.0F * cyyn$ITEM_ICON_SCALE) + cyyn$ITEM_ICON_TEXT_PADDING;
        cyyn$itemPaddingSpaceCount = Math.max(1, Mth.ceil(desiredSpacing / (float) spaceWidth));
        cyyn$itemPaddingWidth = cyyn$itemPaddingSpaceCount * (float) spaceWidth;
    }
}
