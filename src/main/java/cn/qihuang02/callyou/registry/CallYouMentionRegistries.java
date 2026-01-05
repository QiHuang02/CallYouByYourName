package cn.qihuang02.callyou.registry;

import cn.qihuang02.callyou.CallYouByYourName;
import cn.qihuang02.callyou.api.MentionType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(modid = CallYouByYourName.MODID)
public final class CallYouMentionRegistries {
    public static final ResourceKey<Registry<MentionType>> MENTION_TYPE_REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(CallYouByYourName.MODID, "mention_type"));

    @SubscribeEvent
    public static void registerDatapackRegistries(DataPackRegistryEvent.@NotNull NewRegistry event) {
        event.dataPackRegistry(
                MENTION_TYPE_REGISTRY_KEY,
                MentionType.MENTION_TYPE_CODEC,
                MentionType.MENTION_TYPE_CODEC
        );
    }
}
