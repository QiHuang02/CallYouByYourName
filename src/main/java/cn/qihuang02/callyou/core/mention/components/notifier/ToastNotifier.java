package cn.qihuang02.callyou.core.mention.components.notifier;

import cn.qihuang02.callyou.CallYouByYourName;
import cn.qihuang02.callyou.api.MentionCandidate;
import cn.qihuang02.callyou.api.MentionContext;
import cn.qihuang02.callyou.api.components.Notifier;
import cn.qihuang02.callyou.core.network.NetworkHandler;
import cn.qihuang02.callyou.registry.BuiltInCallYouRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.FrameType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record ToastNotifier(
        String titleKey,
        String descriptionKey,
        boolean useSenderName,
        Item icon,
        FrameType toastType
) implements Notifier {

    private static final Codec<FrameType> FRAME_TYPE_CODEC = Codec.STRING.xmap(
            s -> {
                if ("task".equalsIgnoreCase(s)) return FrameType.TASK;
                if ("challenge".equalsIgnoreCase(s)) return FrameType.CHALLENGE;
                if ("goal".equalsIgnoreCase(s)) return FrameType.GOAL;
                return FrameType.TASK;
            },
            frameType -> frameType.getName()
    );

    public static final MapCodec<ToastNotifier> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.optionalFieldOf("title_key", "message.callyou.notify.toast.title").forGetter(ToastNotifier::titleKey),
            Codec.STRING.optionalFieldOf("description_key", "message.callyou.notify.toast.description").forGetter(ToastNotifier::descriptionKey),
            Codec.BOOL.optionalFieldOf("use_sender_name", true).forGetter(ToastNotifier::useSenderName),
            BuiltInRegistries.ITEM.byNameCodec().optionalFieldOf("icon", Items.NAME_TAG).forGetter(ToastNotifier::icon),
            FRAME_TYPE_CODEC.optionalFieldOf("toast_type", FrameType.TASK).forGetter(ToastNotifier::toastType)
    ).apply(instance, ToastNotifier::new));

    private static final ResourceLocation TOAST_ID =
            new ResourceLocation(CallYouByYourName.MODID, "mention_toast");

    @Override
    public @NotNull NotifierType type() {
        return BuiltInCallYouRegistries.TOAST_TYPE;
    }

    @Override
    public void apply(
            @NotNull MentionContext context,
            @NotNull MentionCandidate candidate,
            @NotNull List<ServerPlayer> targets
    ) {
        Component title = buildComponent(titleKey, context);
        Component description = buildComponent(descriptionKey, context);
        Item safeIcon = icon == Items.AIR ? Items.NAME_TAG : icon;
        DisplayInfo display = new DisplayInfo(
                new ItemStack(safeIcon),
                title,
                description,
                null,
                toastType,
                true,
                false,
                true
        );
        Advancement advancement = Advancement.Builder.advancement()
                .display(display)
                .build(TOAST_ID);

        for (ServerPlayer target : targets) {
            NetworkHandler.sendToast(target, advancement);
        }
    }

    private @NotNull Component buildComponent(String key, MentionContext context) {
        if (useSenderName) {
            return Component.translatable(key, context.senderDisplayName());
        }
        return Component.translatable(key);
    }
}
