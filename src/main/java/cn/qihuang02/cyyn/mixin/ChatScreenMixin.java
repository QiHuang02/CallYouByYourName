package cn.qihuang02.cyyn.mixin;

import cn.qihuang02.cyyn.client.chat.MentionSuggestions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {
    @Shadow
    protected EditBox input;

    @Unique
    private MentionSuggestions cyyn$mentionSuggestions;

    @Inject(method = "init", at = @At("TAIL"))
    private void cyyn$init(CallbackInfo ci) {
        this.cyyn$mentionSuggestions = new MentionSuggestions(Minecraft.getInstance());
        this.cyyn$mentionSuggestions.attach(this.input);
        this.cyyn$mentionSuggestions.refresh();
    }

    @Inject(method = "resize", at = @At("TAIL"))
    private void cyyn$resize(Minecraft minecraft, int width, int height, CallbackInfo ci) {
        if (this.cyyn$mentionSuggestions != null) {
            this.cyyn$mentionSuggestions.attach(this.input);
            this.cyyn$mentionSuggestions.refresh();
        }
    }

    @Inject(method = "removed", at = @At("HEAD"))
    private void cyyn$removed(CallbackInfo ci) {
        if (this.cyyn$mentionSuggestions != null) {
            this.cyyn$mentionSuggestions.hide();
        }
    }

    @Inject(method = "onEdited", at = @At("TAIL"))
    private void cyyn$onEdited(String newText, CallbackInfo ci) {
        if (this.cyyn$mentionSuggestions != null) {
            this.cyyn$mentionSuggestions.refresh();
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void cyyn$keyPressedHead(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (this.cyyn$mentionSuggestions != null && this.cyyn$mentionSuggestions.handleKeyPressed(keyCode)) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    @Inject(method = "keyPressed", at = @At("TAIL"))
    private void cyyn$keyPressedTail(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (this.cyyn$mentionSuggestions != null) {
            this.cyyn$mentionSuggestions.refresh();
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void cyyn$mouseClickedHead(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (this.cyyn$mentionSuggestions != null && this.cyyn$mentionSuggestions.mouseClicked(mouseX, mouseY, button)) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    @Inject(method = "mouseClicked", at = @At("TAIL"))
    private void cyyn$mouseClickedTail(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (this.cyyn$mentionSuggestions != null) {
            this.cyyn$mentionSuggestions.refresh();
        }
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void cyyn$mouseScrolledHead(double mouseX, double mouseY, double delta, CallbackInfoReturnable<Boolean> cir) {
        if (this.cyyn$mentionSuggestions != null && this.cyyn$mentionSuggestions.mouseScrolled(delta)) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void cyyn$render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (this.cyyn$mentionSuggestions != null) {
            this.cyyn$mentionSuggestions.render(guiGraphics, mouseX, mouseY, this.input);
        }
    }
}
