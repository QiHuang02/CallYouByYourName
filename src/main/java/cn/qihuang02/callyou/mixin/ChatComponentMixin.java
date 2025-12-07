package cn.qihuang02.callyou.mixin;

import cn.qihuang02.callyou.config.CallYouConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@OnlyIn(Dist.CLIENT)
@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin {

    @Unique
    private static final float CALLYOU_ITEM_ICON_SCALE = 0.6F;
    @Unique
    private static final float CALLYOU_ITEM_ICON_EXTRA_SHIFT = 1.0F;
    @Unique
    private static final String CALLYOU_ITEM_ICON_PLACEHOLDER = "  ";

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;III)I"
            ),
            require = 0
    )
    private int callyou$renderItemIconsBeforeText(
            GuiGraphics guiGraphics,
            Font font,
            FormattedCharSequence line,
            int x,
            int y,
            int color
    ) {
        callyou$renderItemIconsInLine(guiGraphics, font, line, x, y, color);
        return guiGraphics.drawString(font, line, x, y, color);
    }

    @Unique
    private void callyou$renderItemIconsInLine(
            GuiGraphics guiGraphics,
            Font font,
            FormattedCharSequence line,
            int baseX,
            int baseY,
            int color
    ) {
        if (!CallYouConfig.COMMON.renderItemIconAndPlaceholder.get()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc == null) {
            return;
        }

        StringBuilder before = new StringBuilder();
        int halfSpace = font.width("  ") / 2;

        line.accept((index, style, codePoint) -> {
            String soFar = before.toString();

            if (codePoint != ' ' && soFar.endsWith(CALLYOU_ITEM_ICON_PLACEHOLDER)) {
                String beforeText = soFar.substring(0, soFar.length() - CALLYOU_ITEM_ICON_PLACEHOLDER.length());

                float extraShift = -halfSpace;

                callyou$renderSingleItemIcon(guiGraphics, font, beforeText,
                        extraShift, baseX, baseY, style, color);

                return false;
            }

            before.appendCodePoint(codePoint);
            return true;
        });
    }

    @Unique
    private void callyou$renderSingleItemIcon(
            GuiGraphics guiGraphics,
            Font font,
            String beforeText,
            float extraShift,
            int baseX,
            int baseY,
            @NotNull Style style,
            int color
    ) {
        HoverEvent hover = style.getHoverEvent();
        if (hover == null || hover.getAction() != HoverEvent.Action.SHOW_ITEM) {
            return;
        }

        HoverEvent.ItemStackInfo info = hover.getValue(HoverEvent.Action.SHOW_ITEM);
        ItemStack stack = info != null ? info.getItemStack() : ItemStack.EMPTY;
        if (stack.isEmpty()) {
            stack = new ItemStack(Blocks.BARRIER);
        }

        float alpha = (color >> 24 & 0xFF) / 255.0F;
        if (alpha <= 0.0F) {
            return;
        }

        float shift = font.width(beforeText) + extraShift + CALLYOU_ITEM_ICON_EXTRA_SHIFT;

        PoseStack pose = guiGraphics.pose();
        pose.pushPose();

        pose.translate(baseX + shift, baseY - 1, 200.0F);
        pose.scale(CALLYOU_ITEM_ICON_SCALE, CALLYOU_ITEM_ICON_SCALE, 1.0F);

        guiGraphics.renderItem(stack, 0, 0);

        pose.popPose();
    }
}

