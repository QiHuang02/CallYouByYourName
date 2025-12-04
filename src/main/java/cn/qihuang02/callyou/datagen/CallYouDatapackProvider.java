package cn.qihuang02.callyou.datagen;

import cn.qihuang02.callyou.CallYouByYourName;
import cn.qihuang02.callyou.api.MentionRules;
import cn.qihuang02.callyou.api.MentionType;
import cn.qihuang02.callyou.core.impl.formatter.PlayerNameTextFormatter;
import cn.qihuang02.callyou.core.impl.formatter.SimpleTextFormatter;
import cn.qihuang02.callyou.core.impl.notify.SoundNotificationRule;
import cn.qihuang02.callyou.core.impl.target.PlayerNameTargetProvider;
import cn.qihuang02.callyou.core.impl.target.RadiusTargetProvider;
import cn.qihuang02.callyou.registry.CallYouMentionRegistries;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.PackOutput;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
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
                new SoundNotificationRule(
                        SoundEvents.EXPERIENCE_ORB_PICKUP,
                        1.0F,
                        1.0F
                ),
                new MentionRules(
                        0,
                        null,
                        true
                )
        );

        context.register(NEAR_MENTION, nearMention);

        MentionType playerMention = new MentionType(
                new PlayerNameTargetProvider(),
                new PlayerNameTextFormatter(ChatFormatting.YELLOW),
                new SoundNotificationRule(
                        SoundEvents.EXPERIENCE_ORB_PICKUP,
                        1.0F,
                        1.0F
                ),
                MentionRules.DEFAULT
        );

        context.register(PLAYER_MENTION, playerMention);
    }
}
