package cn.qihuang02.callyou.registry;

import cn.qihuang02.callyou.CallYouByYourName;
import cn.qihuang02.callyou.api.components.Notifier;
import cn.qihuang02.callyou.api.components.TargetProvider;
import cn.qihuang02.callyou.api.components.TextFormatter;
import cn.qihuang02.callyou.core.mention.components.formatter.ItemTextFormatter;
import cn.qihuang02.callyou.core.mention.components.formatter.PlayerNameTextFormatter;
import cn.qihuang02.callyou.core.mention.components.formatter.SimpleTextFormatter;
import cn.qihuang02.callyou.core.mention.components.formatter.SpotTextFormatter;
import cn.qihuang02.callyou.core.mention.components.notifier.ActionBarNotifier;
import cn.qihuang02.callyou.core.mention.components.notifier.SoundNotifier;
import cn.qihuang02.callyou.core.mention.components.notifier.ToastNotifier;
import cn.qihuang02.callyou.core.mention.components.targetProvider.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class BuiltInCallYouRegistries {
    public static final DeferredRegister<TargetProvider.TargetProviderType> TARGET_PROVIDER_TYPES =
            DeferredRegister.create(CallYouRegistries.TARGET_PROVIDER_TYPE_REGISTRY_KEY, CallYouByYourName.MODID);

    public static final DeferredRegister<TextFormatter.TextFormatterType> TEXT_FORMATTER_TYPES =
            DeferredRegister.create(CallYouRegistries.TEXT_FORMATTER_TYPE_REGISTRY_KEY, CallYouByYourName.MODID);

    public static final DeferredRegister<Notifier.NotifierType> NOTIFICATION_RULE_TYPES =
            DeferredRegister.create(CallYouRegistries.NOTIFICATION_RULE_TYPE_REGISTRY_KEY, CallYouByYourName.MODID);

    public static final DeferredHolder<TargetProvider.TargetProviderType, TargetProvider.TargetProviderType> RADIUS_TYPE =
            TARGET_PROVIDER_TYPES.register("radius", () -> new TargetProvider.TargetProviderType(RadiusTargetProvider.MAP_CODEC));

    public static final DeferredHolder<TargetProvider.TargetProviderType, TargetProvider.TargetProviderType> PLAYER_NAME_TYPE =
            TARGET_PROVIDER_TYPES.register("player", () -> new TargetProvider.TargetProviderType(PlayerNameTargetProvider.MAP_CODEC));

    public static final DeferredHolder<TargetProvider.TargetProviderType, TargetProvider.TargetProviderType> NONE_TARGET_TYPE =
            TARGET_PROVIDER_TYPES.register("none", () -> new TargetProvider.TargetProviderType(NoneTargetProvider.MAP_CODEC));

    public static final DeferredHolder<TargetProvider.TargetProviderType, TargetProvider.TargetProviderType> DIMENSION_TARGET_TYPE =
            TARGET_PROVIDER_TYPES.register("dimension", () -> new TargetProvider.TargetProviderType(DimensionTargetProvider.MAP_CODEC));

    public static final DeferredHolder<TargetProvider.TargetProviderType, TargetProvider.TargetProviderType> SAME_DIMENSION_TARGET_TYPE =
            TARGET_PROVIDER_TYPES.register("same_dimension", () -> new TargetProvider.TargetProviderType(SameDimensionTargetProvider.MAP_CODEC));

    public static final DeferredHolder<TargetProvider.TargetProviderType, TargetProvider.TargetProviderType> FTB_TEAM_TARGET_TYPE =
            TARGET_PROVIDER_TYPES.register("ftb_team", () -> new TargetProvider.TargetProviderType(FTBTeamTargetProvider.MAP_CODEC));

    public static final DeferredHolder<TextFormatter.TextFormatterType, TextFormatter.TextFormatterType> SIMPLE_FORMATTER_TYPE =
            TEXT_FORMATTER_TYPES.register("simple_formatter", () -> new TextFormatter.TextFormatterType(SimpleTextFormatter.MAP_CODEC));

    public static final DeferredHolder<TextFormatter.TextFormatterType, TextFormatter.TextFormatterType> SPOT_FORMATTER_TYPE =
            TEXT_FORMATTER_TYPES.register("spot", () -> new TextFormatter.TextFormatterType(SpotTextFormatter.MAP_CODEC));

    public static final DeferredHolder<TextFormatter.TextFormatterType, TextFormatter.TextFormatterType> ITEM_FORMATTER_TYPE =
            TEXT_FORMATTER_TYPES.register("item", () -> new TextFormatter.TextFormatterType(ItemTextFormatter.MAP_CODEC));

    public static final DeferredHolder<TextFormatter.TextFormatterType, TextFormatter.TextFormatterType> PLAYER_NAME_FORMATTER_TYPE =
            TEXT_FORMATTER_TYPES.register("player_name", () -> new TextFormatter.TextFormatterType(PlayerNameTextFormatter.MAP_CODEC));

    public static final DeferredHolder<Notifier.NotifierType, Notifier.NotifierType> SOUND_TYPE =
            NOTIFICATION_RULE_TYPES.register("sound", () -> new Notifier.NotifierType(SoundNotifier.MAP_CODEC));

    public static final DeferredHolder<Notifier.NotifierType, Notifier.NotifierType> ACTION_BAR_TYPE =
            NOTIFICATION_RULE_TYPES.register("action_bar", () -> new Notifier.NotifierType(ActionBarNotifier.MAP_CODEC));

    public static final DeferredHolder<Notifier.NotifierType, Notifier.NotifierType> TOAST_TYPE =
            NOTIFICATION_RULE_TYPES.register("toast", () -> new Notifier.NotifierType(ToastNotifier.MAP_CODEC));

    public static void register(IEventBus modEventBus) {
        TARGET_PROVIDER_TYPES.register(modEventBus);
        TEXT_FORMATTER_TYPES.register(modEventBus);
        NOTIFICATION_RULE_TYPES.register(modEventBus);
    }
}
