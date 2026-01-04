package cn.qihuang02.callyou.mixin;

import cn.qihuang02.callyou.client.compat.ftb.FTBChunksClickHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Style;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@OnlyIn(Dist.CLIENT)
@Mixin(Screen.class)
public abstract class ScreenMixin {
    @Inject(method = "handleComponentClicked", at = @At("HEAD"), cancellable = true)
    private void callyou$handleComponentClicked(@Nullable Style style, CallbackInfoReturnable<Boolean> cir) {
        if (style == null) {
            return;
        }

        ClickEvent clickEvent = style.getClickEvent();
        if (clickEvent == null) {
            return;
        }

        if (FTBChunksClickHandler.handle(clickEvent)) {
            cir.setReturnValue(true);
        }
    }
}
