package cn.qihuang02.callyou.core.mention;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public final class ComponentTraversal {
    public static <T> @Nullable T findFirst(
            @NotNull Component root,
            @NotNull Function<Component, T> mapper
    ) {
        Deque<Component> stack = new ArrayDeque<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            Component current = stack.pop();
            T value = mapper.apply(current);
            if (value != null) {
                return value;
            }

            List<Component> siblings = current.getSiblings();
            for (int i = siblings.size() - 1; i >= 0; i--) {
                stack.push(siblings.get(i));
            }

            if (current.getContents() instanceof TranslatableContents translatable) {
                Object[] args = translatable.getArgs();
                for (int i = args.length - 1; i >= 0; i--) {
                    Object arg = args[i];
                    if (arg instanceof Component nested) {
                        stack.push(nested);
                    }
                }
            }
        }
        return null;
    }

    public static <T> @Nullable T findFirstStyleValue(
            @NotNull Component root,
            @NotNull Function<Style, T> mapper
    ) {
        return findFirst(root, component -> mapper.apply(component.getStyle()));
    }

    public static boolean contains(
            @NotNull Component root,
            @NotNull Predicate<Component> predicate
    ) {
        return findFirst(root, component -> predicate.test(component) ? Boolean.TRUE : null) != null;
    }
}
