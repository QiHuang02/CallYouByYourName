package cn.qihuang02.cyyn.mixin;

import cn.qihuang02.cyyn.client.chat.MentionHighlighter;
import cn.qihuang02.cyyn.client.chat.MentionSuggestions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.BiFunction;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {
    @Shadow
    protected EditBox input;

    @Unique
    private MentionSuggestions cyyn$mentionSuggestions;

    @Unique
    private MentionHighlighter cyyn$mentionHighlighter;

    @Unique
    private BiFunction<String, Integer, FormattedCharSequence> cyyn$mentionFormatter;

    @Unique
    private void cyyn$invalidateFormatter() {
        if (this.cyyn$mentionHighlighter != null) {
            this.cyyn$mentionHighlighter.invalidate();
        }
    }

    @Unique
    private void cyyn$resetFormatter() {
        if (this.input != null) {
            this.input.setFormatter(MentionHighlighter::vanillaFormatter);
        }
        this.cyyn$mentionFormatter = null;
        this.cyyn$mentionHighlighter = null;
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void cyyn$init(CallbackInfo ci) {
        this.cyyn$mentionSuggestions = new MentionSuggestions(Minecraft.getInstance());
        this.cyyn$mentionSuggestions.attach(this.input);
        this.cyyn$mentionSuggestions.refresh();

        this.cyyn$mentionHighlighter = new MentionHighlighter(Minecraft.getInstance());
        this.cyyn$mentionFormatter = this.cyyn$mentionHighlighter::format;
        this.input.setFormatter(this.cyyn$mentionFormatter);
        this.cyyn$mentionHighlighter.invalidate();
    }

    @Inject(method = "resize", at = @At("HEAD"))
    private void cyyn$resizeHead(Minecraft minecraft, int width, int height, CallbackInfo ci) {
        cyyn$resetFormatter();
    }

    @Inject(method = "resize", at = @At("TAIL"))
    private void cyyn$resize(Minecraft minecraft, int width, int height, CallbackInfo ci) {
        if (this.cyyn$mentionSuggestions != null) {
            this.cyyn$mentionSuggestions.attach(this.input);
            this.cyyn$mentionSuggestions.refresh();
        }
        if (this.cyyn$mentionHighlighter != null && this.cyyn$mentionFormatter != null) {
            this.input.setFormatter(this.cyyn$mentionFormatter);
            this.cyyn$mentionHighlighter.invalidate();
        }
    }

    @Inject(method = "removed", at = @At("HEAD"))
    private void cyyn$removed(CallbackInfo ci) {
        if (this.cyyn$mentionSuggestions != null) {
            this.cyyn$mentionSuggestions.hide();
        }
        cyyn$resetFormatter();
    }

    @Inject(method = "onEdited", at = @At("TAIL"))
    private void cyyn$onEdited(String newText, CallbackInfo ci) {
        if (this.cyyn$mentionSuggestions != null) {
            this.cyyn$mentionSuggestions.refresh();
        }
        cyyn$invalidateFormatter();
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
        cyyn$invalidateFormatter();
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
        cyyn$invalidateFormatter();
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
