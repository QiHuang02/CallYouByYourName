package cn.qihuang02.callyou.core.client;

import net.minecraft.advancements.Advancement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.AdvancementToast;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public final class ToastNotifierClient {
    public static void showToast(@NotNull Advancement advancement) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getToasts().addToast(new AdvancementToast(advancement));
    }
}
