package cn.qihuang02.callyou.datagen;

import cn.qihuang02.callyou.CallYouByYourName;
import cn.qihuang02.callyou.api.MentionType;
import cn.qihuang02.callyou.api.components.MentionRules;
import cn.qihuang02.callyou.core.mention.components.formatter.ItemTextFormatter;
import cn.qihuang02.callyou.core.mention.components.formatter.PlayerNameTextFormatter;
import cn.qihuang02.callyou.core.mention.components.formatter.SimpleTextFormatter;
import cn.qihuang02.callyou.core.mention.components.formatter.SpotTextFormatter;
import cn.qihuang02.callyou.core.mention.components.notifier.SoundNotifier;
import cn.qihuang02.callyou.core.mention.components.notifier.ToastNotifier;
import cn.qihuang02.callyou.core.mention.components.targetProvider.*;
import cn.qihuang02.callyou.registry.CallYouMentionRegistries;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.PackOutput;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public final class CallYouDatapackProvider extends DatapackBuiltinEntriesProvider {
    private static final ResourceKey<MentionType> NEAR_MENTION = ResourceKey.create(
            CallYouMentionRegistries.MENTION_TYPE_REGISTRY_KEY,
            ResourceLocation.fromNamespaceAndPath(CallYouByYourName.MODID, "near")
    );
    private static final ResourceKey<MentionType> PLAYER_MENTION = ResourceKey.create(
            CallYouMentionRegistries.MENTION_TYPE_REGISTRY_KEY,
            ResourceLocation.fromNamespaceAndPath(CallYouByYourName.MODID, "player")
    );
    private static final ResourceKey<MentionType> ITEM_MENTION = ResourceKey.create(
            CallYouMentionRegistries.MENTION_TYPE_REGISTRY_KEY,
            ResourceLocation.fromNamespaceAndPath(CallYouByYourName.MODID, "item")
    );
    private static final ResourceKey<MentionType> HERE_MENTION = ResourceKey.create(
            CallYouMentionRegistries.MENTION_TYPE_REGISTRY_KEY,
            ResourceLocation.fromNamespaceAndPath(CallYouByYourName.MODID, "here")
    );
    private static final ResourceKey<MentionType> SPOT_MENTION = ResourceKey.create(
            CallYouMentionRegistries.MENTION_TYPE_REGISTRY_KEY,
            ResourceLocation.fromNamespaceAndPath(CallYouByYourName.MODID, "spot")
    );
    private static final ResourceKey<MentionType> FTB_TEAM_MENTION = ResourceKey.create(
            CallYouMentionRegistries.MENTION_TYPE_REGISTRY_KEY,
            ResourceLocation.fromNamespaceAndPath(CallYouByYourName.MODID, "team")
    );
    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(
                    CallYouMentionRegistries.MENTION_TYPE_REGISTRY_KEY,
                    CallYouDatapackProvider::bootstrapMentionTypes
            );

    public CallYouDatapackProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(CallYouByYourName.MODID));
    }

    private static void bootstrapMentionTypes(@NotNull BootstrapContext<MentionType> context) {
        MentionType nearMention = new MentionType(
                new RadiusTargetProvider(32.0D),
                new SimpleTextFormatter("@near", ChatFormatting.AQUA),
                new SoundNotifier(
                        SoundEvents.EXPERIENCE_ORB_PICKUP,
                        1.0F,
                        1.0F
                ),
                new MentionRules(
                        0,
                        true
                )
        );

        context.register(NEAR_MENTION, nearMention);

        MentionType playerMention = new MentionType(
                new PlayerNameTargetProvider(),
                new PlayerNameTextFormatter(ChatFormatting.YELLOW),
                new SoundNotifier(
                        SoundEvents.EXPERIENCE_ORB_PICKUP,
                        1.0F,
                        1.0F
                ),
                MentionRules.DEFAULT
        );

        context.register(PLAYER_MENTION, playerMention);

        MentionType itemMention = new MentionType(
                NoneTargetProvider.INSTANCE,
                ItemTextFormatter.INSTANCE,
                new SoundNotifier(
                        SoundEvents.EXPERIENCE_ORB_PICKUP,
                        1.0F,
                        1.0F
                ),
                MentionRules.DEFAULT
        );
        context.register(ITEM_MENTION, itemMention);

        MentionType hereMention = new MentionType(
                new SameDimensionTargetProvider(),
                new SimpleTextFormatter("@here", ChatFormatting.AQUA),
                new SoundNotifier(
                        SoundEvents.EXPERIENCE_ORB_PICKUP,
                        1.0F,
                        1.0F
                ),
                new MentionRules(
                        0,
                        true
                )
        );
        context.register(HERE_MENTION, hereMention);

        MentionType spotMention = new MentionType(
                NoneTargetProvider.INSTANCE,
                SpotTextFormatter.INSTANCE,
                new SoundNotifier(
                        SoundEvents.EXPERIENCE_ORB_PICKUP,
                        1.0F,
                        1.0F
                ),
                MentionRules.DEFAULT
        );
        context.register(SPOT_MENTION, spotMention);

        MentionType ftbTeamMention = new MentionType(
                new FTBTeamTargetProvider(),
                new SimpleTextFormatter("@team", ChatFormatting.GOLD),
                new ToastNotifier(
                        "message.callyou.notify.toast.title",
                        "message.callyou.notify.toast.description",
                        true,
                        Items.NAME_TAG,
                        AdvancementType.GOAL
                ),
                new MentionRules(
                        0,
                        true
                )
        );

        context.register(FTB_TEAM_MENTION, ftbTeamMention);
    }
}
