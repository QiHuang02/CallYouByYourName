package cn.qihuang02.cyyn.util;

import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Unique;

@Unique
public record ItemHoverArea(int x, int y, int width, int height, Style style) {
    public boolean contains(int pointX, int pointY) {
        return pointX >= x && pointX < x + width && pointY >= y && pointY < y + height;
    }
}
