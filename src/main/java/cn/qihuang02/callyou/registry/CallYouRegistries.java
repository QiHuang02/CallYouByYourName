package cn.qihuang02.callyou.registry;

import cn.qihuang02.callyou.CallYouByYourName;
import cn.qihuang02.callyou.api.NotifierType;
import cn.qihuang02.callyou.api.TargetProviderType;
import cn.qihuang02.callyou.api.TextFormatterType;
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
    public static final ResourceKey<Registry<TargetProviderType>> TARGET_PROVIDER_TYPE_REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(CallYouByYourName.MODID, "target_provider_type"));


    public static final ResourceKey<Registry<TextFormatterType>> TEXT_FORMATTER_TYPE_REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(CallYouByYourName.MODID, "text_formatter_type"));


    public static final ResourceKey<Registry<NotifierType>> NOTIFICATION_RULE_TYPE_REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(CallYouByYourName.MODID, "notification_rule_type"));


    public static final Registry<TargetProviderType> TARGET_PROVIDER_TYPES =
            new RegistryBuilder<>(TARGET_PROVIDER_TYPE_REGISTRY_KEY)
                    .sync(true)
                    .create();


    public static final Registry<TextFormatterType> TEXT_FORMATTER_TYPES =
            new RegistryBuilder<>(TEXT_FORMATTER_TYPE_REGISTRY_KEY)
                    .sync(true)
                    .create();


    public static final Registry<NotifierType> NOTIFICATION_RULE_TYPES =
            new RegistryBuilder<>(NOTIFICATION_RULE_TYPE_REGISTRY_KEY)
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
    }
}
