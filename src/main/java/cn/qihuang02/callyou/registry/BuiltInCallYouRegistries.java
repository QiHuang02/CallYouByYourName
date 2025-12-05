package cn.qihuang02.callyou.registry;

import cn.qihuang02.callyou.CallYouByYourName;
import cn.qihuang02.callyou.api.NotificationRuleType;
import cn.qihuang02.callyou.api.TargetProviderType;
import cn.qihuang02.callyou.api.TextFormatterType;
import cn.qihuang02.callyou.core.impl.formatter.ItemTextFormatter;
import cn.qihuang02.callyou.core.impl.formatter.PlayerNameTextFormatter;
import cn.qihuang02.callyou.core.impl.formatter.SimpleTextFormatter;
import cn.qihuang02.callyou.core.impl.formatter.SpotTextFormatter;
import cn.qihuang02.callyou.core.impl.notify.SoundNotificationRule;
import cn.qihuang02.callyou.core.impl.target.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class BuiltInCallYouRegistries {
    public static final DeferredRegister<TargetProviderType> TARGET_PROVIDER_TYPES =
            DeferredRegister.create(CallYouRegistries.TARGET_PROVIDER_TYPE_REGISTRY_KEY, CallYouByYourName.MODID);

    public static final DeferredRegister<TextFormatterType> TEXT_FORMATTER_TYPES =
            DeferredRegister.create(CallYouRegistries.TEXT_FORMATTER_TYPE_REGISTRY_KEY, CallYouByYourName.MODID);

    public static final DeferredRegister<NotificationRuleType> NOTIFICATION_RULE_TYPES =
            DeferredRegister.create(CallYouRegistries.NOTIFICATION_RULE_TYPE_REGISTRY_KEY, CallYouByYourName.MODID);

    public static final DeferredHolder<TargetProviderType, TargetProviderType> RADIUS_TYPE =
            TARGET_PROVIDER_TYPES.register("radius", () -> new TargetProviderType(RadiusTargetProvider.MAP_CODEC));

    public static final DeferredHolder<TargetProviderType, TargetProviderType> PLAYER_NAME_TYPE =
            TARGET_PROVIDER_TYPES.register("player", () -> new TargetProviderType(PlayerNameTargetProvider.MAP_CODEC));

    public static final DeferredHolder<TargetProviderType, TargetProviderType> NONE_TARGET_TYPE =
            TARGET_PROVIDER_TYPES.register("none", () -> new TargetProviderType(NoneTargetProvider.MAP_CODEC));

    public static final DeferredHolder<TargetProviderType, TargetProviderType> DIMENSION_TARGET_TYPE =
            TARGET_PROVIDER_TYPES.register("dimension", () -> new TargetProviderType(DimensionTargetProvider.MAP_CODEC));

    public static final DeferredHolder<TargetProviderType, TargetProviderType> SAME_DIMENSION_TARGET_TYPE =
            TARGET_PROVIDER_TYPES.register("same_dimension", () -> new TargetProviderType(SameDimensionTargetProvider.MAP_CODEC));

    public static final DeferredHolder<TextFormatterType, TextFormatterType> SIMPLE_FORMATTER_TYPE =
            TEXT_FORMATTER_TYPES.register("simple_formatter", () -> new TextFormatterType(SimpleTextFormatter.MAP_CODEC));

    public static final DeferredHolder<TextFormatterType, TextFormatterType> SPOT_FORMATTER_TYPE =
            TEXT_FORMATTER_TYPES.register("spot", () -> new TextFormatterType(SpotTextFormatter.MAP_CODEC));

    public static final DeferredHolder<TextFormatterType, TextFormatterType> ITEM_FORMATTER_TYPE =
            TEXT_FORMATTER_TYPES.register("item", () -> new TextFormatterType(ItemTextFormatter.MAP_CODEC));

    public static final DeferredHolder<TextFormatterType, TextFormatterType> PLAYER_NAME_FORMATTER_TYPE =
            TEXT_FORMATTER_TYPES.register("player_name", () -> new TextFormatterType(PlayerNameTextFormatter.MAP_CODEC));

    public static final DeferredHolder<NotificationRuleType, NotificationRuleType> SOUND_TYPE =
            NOTIFICATION_RULE_TYPES.register("sound", () -> new NotificationRuleType(SoundNotificationRule.MAP_CODEC));

    public static void register(IEventBus modEventBus) {
        TARGET_PROVIDER_TYPES.register(modEventBus);
        TEXT_FORMATTER_TYPES.register(modEventBus);
        NOTIFICATION_RULE_TYPES.register(modEventBus);
    }
}
