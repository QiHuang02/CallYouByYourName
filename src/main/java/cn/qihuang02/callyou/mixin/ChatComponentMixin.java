package cn.qihuang02.callyou.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.List;

@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin {

    @Shadow @Final
    private Minecraft minecraft;

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;III)I"
            )
    )
    private int callyou$drawStringWithItemIcons(
            @NotNull GuiGraphics graphics,
            Font font,
            FormattedCharSequence text,
            int x,
            int y,
            int color
    ) {
        int result = graphics.drawString(font, text, x, y, color);

        callyou$renderItemIconsInLine(graphics, font, text, x, y);

        return result;
    }

    @Unique
    private void callyou$renderItemIconsInLine(
            @NotNull GuiGraphics graphics,
            @NotNull Font font,
            @NotNull FormattedCharSequence line,
            int baseX,
            int baseY
    ) {
        List<Pair<Integer, ItemStack>> icons = callyou$findItemIcons(font, line, baseX);
        if (icons.isEmpty()) {
            return;
        }

        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(0.0F, 0.0F, 200.0F);

        for (Pair<Integer, ItemStack> icon : icons) {
            int iconX = icon.getFirst();
            ItemStack stack = icon.getSecond();
            if (stack.isEmpty()) continue;

            int iconY = baseY - 4;

            graphics.renderItem(stack, iconX, iconY);
        }

        pose.popPose();
    }

    @Unique
    private @NotNull List<Pair<Integer, ItemStack>> callyou$findItemIcons(
            @NotNull Font font,
            @NotNull FormattedCharSequence line,
            int baseX
    ) {
        List<Pair<Integer, ItemStack>> result = new ArrayList<>();

        var splitter = font.getSplitter();
        int totalWidth = font.width(line);

        HoverEvent.ItemStackInfo currentInfo = null;

        for (int dx = 0; dx < totalWidth; dx++) {
            Style style = splitter.componentStyleAtWidth(line, dx);
            HoverEvent hover = style != null ? style.getHoverEvent() : null;

            HoverEvent.ItemStackInfo info = null;
            if (hover != null && hover.getAction() == HoverEvent.Action.SHOW_ITEM) {
                info = hover.getValue(HoverEvent.Action.SHOW_ITEM);
            }

            if (info != null && info != currentInfo) {
                ItemStack stack = info.getItemStack();
                if (!stack.isEmpty()) {
                    int iconX = baseX + dx - 10;
                    result.add(Pair.of(iconX, stack));
                }
            }

            currentInfo = info;
        }

        return result;
    }
}
