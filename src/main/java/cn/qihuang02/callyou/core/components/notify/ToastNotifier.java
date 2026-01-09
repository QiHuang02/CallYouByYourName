package cn.qihuang02.callyou.core.components.notify;

import cn.qihuang02.callyou.CallYouByYourName;
import cn.qihuang02.callyou.api.MentionContext;
import cn.qihuang02.callyou.api.components.Notifier;
import cn.qihuang02.callyou.network.CYRPCPacket;
import cn.qihuang02.callyou.registry.BuiltInCallYouRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record ToastNotifier(
        String titleKey,
        String descriptionKey,
        boolean useSenderName,
        Item icon,
        AdvancementType toastType
) implements Notifier {
    public static final MapCodec<ToastNotifier> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.optionalFieldOf("title_key", "message.callyou.notify.toast.title").forGetter(ToastNotifier::titleKey),
            Codec.STRING.optionalFieldOf("description_key", "message.callyou.notify.toast.description").forGetter(ToastNotifier::descriptionKey),
            Codec.BOOL.optionalFieldOf("use_sender_name", true).forGetter(ToastNotifier::useSenderName),
            BuiltInRegistries.ITEM.byNameCodec().optionalFieldOf("icon", Items.NAME_TAG).forGetter(ToastNotifier::icon),
            AdvancementType.CODEC.optionalFieldOf("toast_type", AdvancementType.TASK).forGetter(ToastNotifier::toastType)
    ).apply(instance, ToastNotifier::new));

    private static final ResourceLocation TOAST_ID =
            ResourceLocation.fromNamespaceAndPath(CallYouByYourName.MODID, "mention_toast");

    @Override
    public @NotNull NotifierType type() {
        return BuiltInCallYouRegistries.TOAST_TYPE.get();
    }

    @Override
    public void apply(MentionContext context, @NotNull List<ServerPlayer> targets) {
        Component title = buildComponent(titleKey, context);
        Component description = buildComponent(descriptionKey, context);
        Item safeIcon = icon == Items.AIR ? Items.NAME_TAG : icon;
        DisplayInfo display = new DisplayInfo(
                new ItemStack(safeIcon),
                title,
                description,
                Optional.empty(),
                toastType,
                true,
                false,
                true
        );
        Advancement advancement = new Advancement(
                Optional.empty(),
                Optional.of(display),
                AdvancementRewards.EMPTY,
                Map.of(),
                AdvancementRequirements.EMPTY,
                false
        );
        AdvancementHolder holder = new AdvancementHolder(TOAST_ID, advancement);

        for (ServerPlayer target : targets) {
            CYRPCPacket.sendToast(target, holder);
        }
    }

    private @NotNull Component buildComponent(String key, MentionContext context) {
        if (useSenderName) {
            return Component.translatable(key, context.senderDisplayName());
        }
        return Component.translatable(key);
    }
}
