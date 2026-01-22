package cn.qihuang02.callyou.registry;

import cn.qihuang02.callyou.CallYouByYourName;
import cn.qihuang02.callyou.api.components.InteractionDecorator;
import cn.qihuang02.callyou.api.components.Notifier;
import cn.qihuang02.callyou.api.components.TargetProvider;
import cn.qihuang02.callyou.api.components.TextFormatter;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(modid = CallYouByYourName.MODID)
public final class CallYouRegistries {
    public static final ResourceKey<Registry<TargetProvider.TargetProviderType>> TARGET_PROVIDER_TYPE_REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(CallYouByYourName.MODID, "target_provider_type"));

    public static final ResourceKey<Registry<TextFormatter.TextFormatterType>> TEXT_FORMATTER_TYPE_REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(CallYouByYourName.MODID, "text_formatter_type"));

    public static final ResourceKey<Registry<Notifier.NotifierType>> NOTIFICATION_RULE_TYPE_REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(CallYouByYourName.MODID, "notifier_type"));

    public static final ResourceKey<Registry<InteractionDecorator.InteractionDecoratorType>> INTERACTION_DECORATOR_TYPE_REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(CallYouByYourName.MODID, "interaction_decorator_type"));

    public static final Registry<TargetProvider.TargetProviderType> TARGET_PROVIDER_TYPES =
            new RegistryBuilder<>(TARGET_PROVIDER_TYPE_REGISTRY_KEY)
                    .sync(true)
                    .create();

    public static final Registry<TextFormatter.TextFormatterType> TEXT_FORMATTER_TYPES =
            new RegistryBuilder<>(TEXT_FORMATTER_TYPE_REGISTRY_KEY)
                    .sync(true)
                    .create();

    public static final Registry<Notifier.NotifierType> NOTIFICATION_RULE_TYPES =
            new RegistryBuilder<>(NOTIFICATION_RULE_TYPE_REGISTRY_KEY)
                    .sync(true)
                    .create();

    public static final Registry<InteractionDecorator.InteractionDecoratorType> INTERACTION_DECORATOR_TYPES =
            new RegistryBuilder<>(INTERACTION_DECORATOR_TYPE_REGISTRY_KEY)
                    .sync(true)
                    .create();

    /**
     * Register the custom registries themselves to the root registry.
     */
    @SubscribeEvent
    public static void registerRegistries(@NotNull NewRegistryEvent event) {
        event.register(TARGET_PROVIDER_TYPES);
        event.register(TEXT_FORMATTER_TYPES);
        event.register(NOTIFICATION_RULE_TYPES);
        event.register(INTERACTION_DECORATOR_TYPES);
    }
}
