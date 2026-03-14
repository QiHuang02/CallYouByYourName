package cn.qihuang02.callyou.registry;

import cn.qihuang02.callyou.CallYouByYourName;
import cn.qihuang02.callyou.api.components.InteractionDecorator;
import cn.qihuang02.callyou.api.components.Notifier;
import cn.qihuang02.callyou.api.components.TargetProvider;
import cn.qihuang02.callyou.api.components.TextFormatter;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class CallYouRegistries {
    private static final Map<ResourceLocation, TargetProvider.TargetProviderType> TARGET_PROVIDER_TYPES =
            new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, TextFormatter.TextFormatterType> TEXT_FORMATTER_TYPES =
            new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, Notifier.NotifierType> NOTIFICATION_RULE_TYPES =
            new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, InteractionDecorator.InteractionDecoratorType> INTERACTION_DECORATOR_TYPES =
            new ConcurrentHashMap<>();

    private CallYouRegistries() {}

    public static <T> void register(Map<ResourceLocation, T> registry, ResourceLocation id, T value) {
        if (registry.putIfAbsent(id, value) != null) {
            throw new IllegalStateException("Duplicate registration: " + id);
        }
    }

    public static <T> void register(Map<ResourceLocation, T> registry, String name, T value) {
        register(registry, new ResourceLocation(CallYouByYourName.MODID, name), value);
    }

    public static Map<ResourceLocation, TargetProvider.TargetProviderType> targetProviderTypes() {
        return Collections.unmodifiableMap(TARGET_PROVIDER_TYPES);
    }

    public static Map<ResourceLocation, TextFormatter.TextFormatterType> textFormatterTypes() {
        return Collections.unmodifiableMap(TEXT_FORMATTER_TYPES);
    }

    public static Map<ResourceLocation, Notifier.NotifierType> notifierTypes() {
        return Collections.unmodifiableMap(NOTIFICATION_RULE_TYPES);
    }

    public static Map<ResourceLocation, InteractionDecorator.InteractionDecoratorType> interactionDecoratorTypes() {
        return Collections.unmodifiableMap(INTERACTION_DECORATOR_TYPES);
    }

    public static Map<ResourceLocation, TargetProvider.TargetProviderType> targetProviderTypesMutable() {
        return TARGET_PROVIDER_TYPES;
    }

    public static Map<ResourceLocation, TextFormatter.TextFormatterType> textFormatterTypesMutable() {
        return TEXT_FORMATTER_TYPES;
    }

    public static Map<ResourceLocation, Notifier.NotifierType> notifierTypesMutable() {
        return NOTIFICATION_RULE_TYPES;
    }

    public static Map<ResourceLocation, InteractionDecorator.InteractionDecoratorType> interactionDecoratorTypesMutable() {
        return INTERACTION_DECORATOR_TYPES;
    }
}
