package cn.qihuang02.callyou.mixin;

import cn.qihuang02.callyou.config.CallYouConfig;
import cn.qihuang02.callyou.core.client.render.ItemIconRenderUtil;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@OnlyIn(Dist.CLIENT)
@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin {
    @WrapOperation(
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
            int color,
            @NotNull Operation<Integer> original
    ) {
        ItemIconRenderUtil.renderItemIconsInLine(guiGraphics, font, line, x, y, color);
        FormattedCharSequence padded = ItemIconRenderUtil.padItemIconText(line, font);
        return original.call(guiGraphics, font, padded, x, y, color);
    }

    @WrapOperation(
            method = "getClickedComponentStyleAt",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/StringSplitter;componentStyleAtWidth(Lnet/minecraft/util/FormattedCharSequence;I)Lnet/minecraft/network/chat/Style;"
            ),
            require = 0
    )
    private Style callyou$expandHoverRangeForItemIcons(
            StringSplitter splitter,
            FormattedCharSequence line,
            int width,
            @NotNull Operation<Style> original
    ) {
        if (CallYouConfig.CLIENT.itemIconRenderMode.get() != CallYouConfig.ItemIconRenderMode.INLINE) {
            return original.call(splitter, line, width);
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return original.call(splitter, line, width);
        }
        Font font = minecraft.font;
        FormattedCharSequence padded = ItemIconRenderUtil.padItemIconText(line, font);
        return original.call(splitter, padded, width);
    }
}
