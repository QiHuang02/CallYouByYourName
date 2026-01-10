package cn.qihuang02.callyou.core.client;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.AdvancementToast;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public final class ToastNotifierClient {
    public static void showToast(@NotNull AdvancementHolder advancement) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getToasts().addToast(new AdvancementToast(advancement));
    }
}
