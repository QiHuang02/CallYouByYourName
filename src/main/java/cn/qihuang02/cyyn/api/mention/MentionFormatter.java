package cn.qihuang02.cyyn.api.mention;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Defines the contract for transforming chat components that contain mention tokens.
 */
public interface MentionFormatter {
    @NotNull
    Result format(@NotNull ServerPlayer sender, @NotNull Component message);

    record Result(@Nullable Component component, boolean cancelEvent) {
        public static @NotNull Result pass() {
            return new Result(null, false);
        }

        public static @NotNull Result cancel() {
            return new Result(null, true);
        }

        public static @NotNull Result replace(@NotNull Component component) {
            return new Result(component, false);
        }

        public boolean replaced() {
            return this.component != null;
        }
    }
}
