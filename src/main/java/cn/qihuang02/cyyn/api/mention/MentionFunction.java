package cn.qihuang02.cyyn.api.mention;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Defines the contract for transforming chat components that contain mention names.
 */
public interface MentionFunction extends Mention {

    @NotNull
    Result format(@NotNull ServerPlayer sender, @NotNull Component message);

    @Override
    default int permissionLevel() {
        return 0;
    }

    @Override
    default @NotNull ChatFormatting pointColor() {
        return ChatFormatting.GOLD;
    }

    record Result(@NotNull String name, @Nullable Component component, boolean cancelEvent, boolean handled) {
        public Result {
            Objects.requireNonNull(name, "name");
        }

        public static @NotNull Result pass(@NotNull String name) {
            return new Result(name, null, false, false);
        }

        public static @NotNull Result cancel(@NotNull String name) {
            return new Result(name, null, true, true);
        }

        public static @NotNull Result replace(@NotNull String name, @NotNull Component component) {
            return new Result(name, component, false, true);
        }

        public static @NotNull Result handled(@NotNull String name) {
            return new Result(name, null, false, true);
        }

        public boolean replaced() {
            return this.component != null;
        }
    }
}
