package cn.qihuang02.callyou.api.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Base interface for dispatched (polymorphic) components that use a type-based codec dispatch pattern.
 * <p>
 * In Forge 1.20.1, we use a hand-rolled Map-based registry instead of NeoForge's custom Registry system.
 * Each component type is registered in a {@code Map<ResourceLocation, T>} and the dispatch codec
 * looks up the type by its ResourceLocation key.
 *
 * @param <C> the component type
 * @param <T> the type descriptor type
 */
public interface IDispatchedComponent<C extends IDispatchedComponent<C, T>, T extends IDispatchedComponent.Type<C>> {

    /**
     * Creates a dispatch codec that uses a Map-based registry to look up component types.
     *
     * @param registrySupplier supplier for the map of ResourceLocation -> Type
     * @param <C>              the component type
     * @param <T>              the type descriptor type
     * @return a Codec that dispatches based on the "type" field
     */
    @SuppressWarnings("unchecked")
    static <C extends IDispatchedComponent<C, T>, T extends IDispatchedComponent.Type<C>>
    Codec<C> codec(@NotNull Supplier<Map<ResourceLocation, T>> registrySupplier) {
        return ResourceLocation.CODEC.<C>dispatch("type",
                component -> {
                    Map<ResourceLocation, T> registry = registrySupplier.get();
                    T componentType = component.type();
                    for (Map.Entry<ResourceLocation, T> entry : registry.entrySet()) {
                        if (entry.getValue().equals(componentType)) {
                            return entry.getKey();
                        }
                    }
                    throw new IllegalStateException("Unregistered component type: " + componentType);
                },
                id -> {
                    Map<ResourceLocation, T> registry = registrySupplier.get();
                    T type = registry.get(id);
                    if (type == null) {
                        throw new IllegalStateException("Unknown component type: " + id);
                    }
                    return (Codec<C>) type.mapCodec().codec();
                }
        );
    }

    T type();

    interface Type<C> {
        MapCodec<? extends C> mapCodec();

        @SuppressWarnings("unchecked")
        default Codec<C> codec() {
            return (Codec<C>) mapCodec().codec();
        }
    }
}
