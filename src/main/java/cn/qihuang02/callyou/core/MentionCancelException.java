package cn.qihuang02.callyou.core;

import net.minecraft.network.chat.Component;

public class MentionCancelException extends RuntimeException {
    private final Component reason;

    public MentionCancelException(Component reason) {
        this.reason = reason;
    }

    public Component getReason() {
        return reason;
    }
}
