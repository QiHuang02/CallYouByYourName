package cn.qihuang02.callyou.api;

import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.NotNull;

public enum ResolveStatus {
    PENDING(false, false, false),
    SUCCESS(true, false, true),
    SUCCESS_OFFLINE(true, false, true),
    NOT_FOUND(false, true, true),
    NOT_FOUND_IGNORED(false, false, false),
    NO_PERMISSION(false, true, true),
    BLOCKED(false, true, false),
    OFFLINE_IGNORED(false, true, false),
    EMPTY_GROUP(false, true, false);

    private final boolean success;
    private final boolean error;
    private final boolean countsTowardsLimit;

    ResolveStatus(boolean success, boolean error, boolean countsTowardsLimit) {
        this.success = success;
        this.error = error;
        this.countsTowardsLimit = countsTowardsLimit;
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isError() {
        return error;
    }

    public boolean countsTowardsLimit() {
        return countsTowardsLimit;
    }

    public @NotNull ChatFormatting getSenderColor() {
        return switch (this) {
            case SUCCESS -> ChatFormatting.GREEN;
            case SUCCESS_OFFLINE -> ChatFormatting.YELLOW;
            case NOT_FOUND, NO_PERMISSION -> ChatFormatting.RED;
            case BLOCKED, OFFLINE_IGNORED, EMPTY_GROUP -> ChatFormatting.DARK_GRAY;
            case NOT_FOUND_IGNORED, PENDING -> ChatFormatting.GRAY;
        };
    }
}
