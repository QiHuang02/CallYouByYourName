package cn.qihuang02.callyou.api;

import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.NotNull;

public enum DeliveryStatus {
    PENDING,
    SENT,
    SKIPPED,
    RATE_LIMITED;

    public @NotNull ChatFormatting getSenderColor() {
        switch (this) {
            case SENT:
                return ChatFormatting.GREEN;
            case SKIPPED:
            case PENDING:
                return ChatFormatting.GRAY;
            case RATE_LIMITED:
                return ChatFormatting.RED;
            default:
                return ChatFormatting.WHITE;
        }
    }
}
