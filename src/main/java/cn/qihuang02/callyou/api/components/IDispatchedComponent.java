package cn.qihuang02.callyou.api.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import org.jetbrains.annotations.NotNull;

public interface IDispatchedComponent<C extends IDispatchedComponent<C, T>, T extends IDispatchedComponent.Type<C>> {
    T type();

    interface Type<C> {
        MapCodec<? extends C> mapCodec();

        @SuppressWarnings("unchecked")
        default Codec<C> codec() {
            return (Codec<C>) mapCodec().codec();
        }
    }

    static <C extends IDispatchedComponent<C, T>, T extends IDispatchedComponent.Type<C>>
    Codec<C> codec(@NotNull Registry<T> registry) {
        return registry.byNameCodec().dispatch("type", IDispatchedComponent::type, Type::mapCodec);
    }
}
