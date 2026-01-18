package cn.qihuang02.callyou.api;

import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.NotNull;

public enum DeliveryStatus {
    PENDING,
    SENT,
    SKIPPED,
    RATE_LIMITED;

    public @NotNull ChatFormatting getSenderColor() {
        return switch (this) {
            case SENT -> ChatFormatting.GREEN;
            case SKIPPED, PENDING -> ChatFormatting.GRAY;
            case RATE_LIMITED -> ChatFormatting.RED;
        };
    }
}
