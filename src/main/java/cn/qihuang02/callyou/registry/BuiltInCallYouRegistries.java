package cn.qihuang02.callyou.registry;

import cn.qihuang02.callyou.CallYouByYourName;
import cn.qihuang02.callyou.api.MentionType;
import cn.qihuang02.callyou.api.components.InteractionDecorator;
import cn.qihuang02.callyou.api.components.MentionRules;
import cn.qihuang02.callyou.api.components.Notifier;
import cn.qihuang02.callyou.api.components.TargetProvider;
import cn.qihuang02.callyou.api.components.TextFormatter;
import cn.qihuang02.callyou.core.mention.components.decorator.*;
import cn.qihuang02.callyou.core.mention.components.formatter.*;
import cn.qihuang02.callyou.core.mention.components.notifier.ActionBarNotifier;
import cn.qihuang02.callyou.core.mention.components.notifier.SoundNotifier;
import cn.qihuang02.callyou.core.mention.components.notifier.ToastNotifier;
import cn.qihuang02.callyou.core.mention.components.targetProvider.*;
import cn.qihuang02.callyou.api.components.NoopInteractionDecorator;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.FrameType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.Map;

public final class BuiltInCallYouRegistries {

    private BuiltInCallYouRegistries() {}

    // TargetProvider types
    public static TargetProvider.TargetProviderType RADIUS_TYPE;
    public static TargetProvider.TargetProviderType PLAYER_NAME_TYPE;
    public static TargetProvider.TargetProviderType NONE_TARGET_TYPE;
    public static TargetProvider.TargetProviderType DIMENSION_TARGET_TYPE;
    public static TargetProvider.TargetProviderType SAME_DIMENSION_TARGET_TYPE;
    public static TargetProvider.TargetProviderType FTB_TEAM_TARGET_TYPE;

    // TextFormatter types
    public static TextFormatter.TextFormatterType SIMPLE_FORMATTER_TYPE;
    public static TextFormatter.TextFormatterType MODULAR_FORMATTER_TYPE;
    public static TextFormatter.TextFormatterType SPOT_FORMATTER_TYPE;
    public static TextFormatter.TextFormatterType ITEM_FORMATTER_TYPE;
    public static TextFormatter.TextFormatterType PLAYER_NAME_FORMATTER_TYPE;

    // Notifier types
    public static Notifier.NotifierType SOUND_TYPE;
    public static Notifier.NotifierType ACTION_BAR_TYPE;
    public static Notifier.NotifierType TOAST_TYPE;

    // InteractionDecorator types
    public static InteractionDecorator.InteractionDecoratorType NOOP_DECORATOR_TYPE;
    public static InteractionDecorator.InteractionDecoratorType COMPOSITE_DECORATOR_TYPE;
    public static InteractionDecorator.InteractionDecoratorType TEXT_COLOR_DECORATOR_TYPE;
    public static InteractionDecorator.InteractionDecoratorType TEXT_STYLE_DECORATOR_TYPE;
    public static InteractionDecorator.InteractionDecoratorType PREFIX_SUFFIX_DECORATOR_TYPE;
    public static InteractionDecorator.InteractionDecoratorType SQUARE_BRACKETS_DECORATOR_TYPE;
    public static InteractionDecorator.InteractionDecoratorType HOVER_TEXT_DECORATOR_TYPE;
    public static InteractionDecorator.InteractionDecoratorType HOVER_ENTITY_DECORATOR_TYPE;
    public static InteractionDecorator.InteractionDecoratorType CLICK_RUN_DECORATOR_TYPE;
    public static InteractionDecorator.InteractionDecoratorType CLICK_SUGGEST_DECORATOR_TYPE;
    public static InteractionDecorator.InteractionDecoratorType OPEN_URL_DECORATOR_TYPE;
    public static InteractionDecorator.InteractionDecoratorType ITEM_RENDER_DECORATOR_TYPE;
    public static InteractionDecorator.InteractionDecoratorType RARITY_STYLE_DECORATOR_TYPE;
    public static InteractionDecorator.InteractionDecoratorType REPLY_DECORATOR_TYPE;

    public static void registerAll() {
        registerTargetProviders();
        registerTextFormatters();
        registerNotifiers();
        registerInteractionDecorators();
        registerMentionTypes();
    }

    private static void registerTargetProviders() {
        Map<ResourceLocation, TargetProvider.TargetProviderType> reg = CallYouRegistries.targetProviderTypesMutable();
        RADIUS_TYPE = new TargetProvider.TargetProviderType(RadiusTargetProvider.MAP_CODEC);
        PLAYER_NAME_TYPE = new TargetProvider.TargetProviderType(PlayerNameTargetProvider.MAP_CODEC);
        NONE_TARGET_TYPE = new TargetProvider.TargetProviderType(NoneTargetProvider.MAP_CODEC);
        DIMENSION_TARGET_TYPE = new TargetProvider.TargetProviderType(DimensionTargetProvider.MAP_CODEC);
        SAME_DIMENSION_TARGET_TYPE = new TargetProvider.TargetProviderType(SameDimensionTargetProvider.MAP_CODEC);
        FTB_TEAM_TARGET_TYPE = new TargetProvider.TargetProviderType(FTBTeamTargetProvider.MAP_CODEC);
        CallYouRegistries.register(reg, "radius", RADIUS_TYPE);
        CallYouRegistries.register(reg, "player", PLAYER_NAME_TYPE);
        CallYouRegistries.register(reg, "none", NONE_TARGET_TYPE);
        CallYouRegistries.register(reg, "dimension", DIMENSION_TARGET_TYPE);
        CallYouRegistries.register(reg, "same_dimension", SAME_DIMENSION_TARGET_TYPE);
        CallYouRegistries.register(reg, "ftb_team", FTB_TEAM_TARGET_TYPE);
    }

    private static void registerTextFormatters() {
        Map<ResourceLocation, TextFormatter.TextFormatterType> reg = CallYouRegistries.textFormatterTypesMutable();
        SIMPLE_FORMATTER_TYPE = new TextFormatter.TextFormatterType(SimpleTextFormatter.MAP_CODEC);
        MODULAR_FORMATTER_TYPE = new TextFormatter.TextFormatterType(ModularTextFormatter.MAP_CODEC);
        SPOT_FORMATTER_TYPE = new TextFormatter.TextFormatterType(SpotTextFormatter.MAP_CODEC);
        ITEM_FORMATTER_TYPE = new TextFormatter.TextFormatterType(ItemTextFormatter.MAP_CODEC);
        PLAYER_NAME_FORMATTER_TYPE = new TextFormatter.TextFormatterType(PlayerNameTextFormatter.MAP_CODEC);
        CallYouRegistries.register(reg, "simple_formatter", SIMPLE_FORMATTER_TYPE);
        CallYouRegistries.register(reg, "modular", MODULAR_FORMATTER_TYPE);
        CallYouRegistries.register(reg, "spot", SPOT_FORMATTER_TYPE);
        CallYouRegistries.register(reg, "item", ITEM_FORMATTER_TYPE);
        CallYouRegistries.register(reg, "player_name", PLAYER_NAME_FORMATTER_TYPE);
    }

    private static void registerNotifiers() {
        Map<ResourceLocation, Notifier.NotifierType> reg = CallYouRegistries.notifierTypesMutable();
        SOUND_TYPE = new Notifier.NotifierType(SoundNotifier.MAP_CODEC);
        ACTION_BAR_TYPE = new Notifier.NotifierType(ActionBarNotifier.MAP_CODEC);
        TOAST_TYPE = new Notifier.NotifierType(ToastNotifier.MAP_CODEC);
        CallYouRegistries.register(reg, "sound", SOUND_TYPE);
        CallYouRegistries.register(reg, "action_bar", ACTION_BAR_TYPE);
        CallYouRegistries.register(reg, "toast", TOAST_TYPE);
    }

    private static void registerInteractionDecorators() {
        Map<ResourceLocation, InteractionDecorator.InteractionDecoratorType> reg = CallYouRegistries.interactionDecoratorTypesMutable();
        NOOP_DECORATOR_TYPE = new InteractionDecorator.InteractionDecoratorType(NoopInteractionDecorator.MAP_CODEC);
        COMPOSITE_DECORATOR_TYPE = new InteractionDecorator.InteractionDecoratorType(CompositeInteractionDecorator.MAP_CODEC);
        TEXT_COLOR_DECORATOR_TYPE = new InteractionDecorator.InteractionDecoratorType(TextColorDecorator.MAP_CODEC);
        TEXT_STYLE_DECORATOR_TYPE = new InteractionDecorator.InteractionDecoratorType(TextStyleDecorator.MAP_CODEC);
        PREFIX_SUFFIX_DECORATOR_TYPE = new InteractionDecorator.InteractionDecoratorType(PrefixSuffixDecorator.MAP_CODEC);
        SQUARE_BRACKETS_DECORATOR_TYPE = new InteractionDecorator.InteractionDecoratorType(SquareBracketsDecorator.MAP_CODEC);
        HOVER_TEXT_DECORATOR_TYPE = new InteractionDecorator.InteractionDecoratorType(HoverTextDecorator.MAP_CODEC);
        HOVER_ENTITY_DECORATOR_TYPE = new InteractionDecorator.InteractionDecoratorType(HoverEntityDecorator.MAP_CODEC);
        CLICK_RUN_DECORATOR_TYPE = new InteractionDecorator.InteractionDecoratorType(ClickRunDecorator.MAP_CODEC);
        CLICK_SUGGEST_DECORATOR_TYPE = new InteractionDecorator.InteractionDecoratorType(ClickSuggestDecorator.MAP_CODEC);
        OPEN_URL_DECORATOR_TYPE = new InteractionDecorator.InteractionDecoratorType(OpenUrlDecorator.MAP_CODEC);
        ITEM_RENDER_DECORATOR_TYPE = new InteractionDecorator.InteractionDecoratorType(ItemRenderDecorator.MAP_CODEC);
        RARITY_STYLE_DECORATOR_TYPE = new InteractionDecorator.InteractionDecoratorType(RarityStyleDecorator.MAP_CODEC);
        REPLY_DECORATOR_TYPE = new InteractionDecorator.InteractionDecoratorType(ReplyDecorator.MAP_CODEC);
        CallYouRegistries.register(reg, "none", NOOP_DECORATOR_TYPE);
        CallYouRegistries.register(reg, "composite", COMPOSITE_DECORATOR_TYPE);
        CallYouRegistries.register(reg, "text_color", TEXT_COLOR_DECORATOR_TYPE);
        CallYouRegistries.register(reg, "text_style", TEXT_STYLE_DECORATOR_TYPE);
        CallYouRegistries.register(reg, "prefix_suffix", PREFIX_SUFFIX_DECORATOR_TYPE);
        CallYouRegistries.register(reg, "square_brackets", SQUARE_BRACKETS_DECORATOR_TYPE);
        CallYouRegistries.register(reg, "hover_text", HOVER_TEXT_DECORATOR_TYPE);
        CallYouRegistries.register(reg, "hover_entity", HOVER_ENTITY_DECORATOR_TYPE);
        CallYouRegistries.register(reg, "click_run", CLICK_RUN_DECORATOR_TYPE);
        CallYouRegistries.register(reg, "click_suggest", CLICK_SUGGEST_DECORATOR_TYPE);
        CallYouRegistries.register(reg, "open_url", OPEN_URL_DECORATOR_TYPE);
        CallYouRegistries.register(reg, "item_render", ITEM_RENDER_DECORATOR_TYPE);
        CallYouRegistries.register(reg, "rarity_style", RARITY_STYLE_DECORATOR_TYPE);
        CallYouRegistries.register(reg, "reply", REPLY_DECORATOR_TYPE);
    }

    private static void registerMentionTypes() {
        // @near - radius 32, mass mention
        CallYouMentionRegistries.register(
                new ResourceLocation(CallYouByYourName.MODID, "near"),
                new MentionType(
                        new RadiusTargetProvider(32.0D),
                        new SimpleTextFormatter("@near", new TextColorDecorator(ChatFormatting.AQUA)),
                        new SoundNotifier(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F),
                        new MentionRules(0, true)
                )
        );

        // @player - player name mention
        CallYouMentionRegistries.register(
                new ResourceLocation(CallYouByYourName.MODID, "player"),
                new MentionType(
                        new PlayerNameTargetProvider(),
                        new PlayerNameTextFormatter(new CompositeInteractionDecorator(List.of(
                                new TextColorDecorator(ChatFormatting.YELLOW),
                                new ReplyDecorator(ReplyDecorator.ReplyScope.TARGET)
                        ))),
                        new SoundNotifier(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F),
                        MentionRules.DEFAULT
                )
        );

        // @item - item sharing (no target)
        CallYouMentionRegistries.register(
                new ResourceLocation(CallYouByYourName.MODID, "item"),
                new MentionType(
                        NoneTargetProvider.INSTANCE,
                        new ItemTextFormatter(new CompositeInteractionDecorator(List.of(
                                ItemRenderDecorator.INSTANCE,
                                RarityStyleDecorator.INSTANCE,
                                SquareBracketsDecorator.INSTANCE
                        ))),
                        new SoundNotifier(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F),
                        MentionRules.DEFAULT
                )
        );

        // @here - same dimension, mass mention
        CallYouMentionRegistries.register(
                new ResourceLocation(CallYouByYourName.MODID, "here"),
                new MentionType(
                        new SameDimensionTargetProvider(),
                        new SimpleTextFormatter("@here", new TextColorDecorator(ChatFormatting.AQUA)),
                        new SoundNotifier(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F),
                        new MentionRules(0, true)
                )
        );

        // @spot - coordinate sharing (no target)
        CallYouMentionRegistries.register(
                new ResourceLocation(CallYouByYourName.MODID, "spot"),
                new MentionType(
                        NoneTargetProvider.INSTANCE,
                        new SpotTextFormatter(new CompositeInteractionDecorator(List.of(
                                new TextColorDecorator(ChatFormatting.GREEN),
                                SquareBracketsDecorator.INSTANCE
                        ))),
                        new SoundNotifier(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F),
                        MentionRules.DEFAULT
                )
        );

        // @team - FTB team mention, mass mention with toast
        CallYouMentionRegistries.register(
                new ResourceLocation(CallYouByYourName.MODID, "team"),
                new MentionType(
                        new FTBTeamTargetProvider(),
                        new SimpleTextFormatter("@team", new TextColorDecorator(ChatFormatting.GOLD)),
                        new ToastNotifier(
                                "message.callyou.notify.toast.title",
                                "message.callyou.notify.toast.description",
                                true,
                                Items.NAME_TAG,
                                FrameType.GOAL
                        ),
                        new MentionRules(0, true)
                )
        );
    }
}
