package cn.qihuang02.callyou.mixin;

import cn.qihuang02.callyou.client.chat.MentionSuggestions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin extends Screen {
    @Shadow
    protected EditBox input;

    @Unique
    private MentionSuggestions callyou$mentionSuggestions;

    protected ChatScreenMixin() {
        super(Component.empty());
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void callyou$init(CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        this.callyou$mentionSuggestions =
                MentionSuggestions.create(minecraft, (ChatScreen) (Object) this, this.input);
    }

    @Inject(method = "resize", at = @At("TAIL"))
    private void callyou$onResize(Minecraft minecraft, int width, int height, CallbackInfo ci) {
        if (this.input == null) {
            return;
        }
        this.callyou$mentionSuggestions =
                MentionSuggestions.create(minecraft, (ChatScreen) (Object) this, this.input);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void callyou$render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (this.callyou$mentionSuggestions != null) {
            this.callyou$mentionSuggestions.renderWithUpdate(graphics, mouseX, mouseY);
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void callyou$keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (this.callyou$mentionSuggestions != null
                && this.callyou$mentionSuggestions.keyPressed(keyCode, scanCode, modifiers)) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void callyou$mouseScrolled(double mouseX, double mouseY,
                                       double scrollX, double scrollY,
                                       CallbackInfoReturnable<Boolean> cir) {
        if (this.callyou$mentionSuggestions != null
                && this.callyou$mentionSuggestions.mouseScrolled(scrollY)) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void callyou$mouseClicked(double mouseX, double mouseY, int button,
                                      CallbackInfoReturnable<Boolean> cir) {
        if (this.callyou$mentionSuggestions != null
                && this.callyou$mentionSuggestions.mouseClicked(mouseX, mouseY, button)) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    @Inject(method = "onEdited", at = @At("TAIL"))
    private void callyou$onEdited(String value, CallbackInfo ci) {
        if (this.callyou$mentionSuggestions != null) {
            this.callyou$mentionSuggestions.markDirty();
        }
    }

    @Inject(method = "removed", at = @At("TAIL"))
    private void callyou$removed(CallbackInfo ci) {
        if (this.callyou$mentionSuggestions != null) {
            this.callyou$mentionSuggestions.hide();
            this.callyou$mentionSuggestions = null;
        }
    }
}
