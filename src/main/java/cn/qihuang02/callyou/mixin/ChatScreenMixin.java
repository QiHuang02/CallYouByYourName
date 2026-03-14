package cn.qihuang02.callyou.mixin;

import cn.qihuang02.callyou.core.client.chat.MentionSuggestions;
import cn.qihuang02.callyou.core.client.render.ItemIconRenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
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
        Font font = minecraft.font;
        ChatScreen self = (ChatScreen) (Object) this;

        this.callyou$mentionSuggestions = new MentionSuggestions(
                minecraft,
                self,
                this.input,
                font
        );
    }

    @Inject(method = "onEdited", at = @At("TAIL"))
    private void callyou$onEdited(String newText, CallbackInfo ci) {
        if (this.callyou$mentionSuggestions != null) {
            this.callyou$mentionSuggestions.markDirty();
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void callyou$render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        ItemIconRenderUtil.renderHoverItemIcon(graphics, this.minecraft, mouseX, mouseY);
        if (this.callyou$mentionSuggestions != null) {
            this.callyou$mentionSuggestions.renderWithUpdate(graphics, mouseX, mouseY);
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void callyou$keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (this.callyou$mentionSuggestions != null
                && this.callyou$mentionSuggestions.keyPressed(keyCode, scanCode, modifiers)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void callyou$mouseScrolled(double mouseX, double mouseY, double delta, CallbackInfoReturnable<Boolean> cir) {
        if (this.callyou$mentionSuggestions != null && this.callyou$mentionSuggestions.isSuggestionsVisible()) {
            double clamped = Mth.clamp(delta, -1.0D, 1.0D);
            if (this.callyou$mentionSuggestions.mouseScrolled(mouseX, mouseY, clamped)) {
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void callyou$mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (this.callyou$mentionSuggestions != null
                && this.callyou$mentionSuggestions.mouseClicked(mouseX, mouseY, button)) {
            cir.setReturnValue(true);
        }
    }
}
