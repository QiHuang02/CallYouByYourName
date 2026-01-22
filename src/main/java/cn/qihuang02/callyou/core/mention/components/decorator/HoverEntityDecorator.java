package cn.qihuang02.callyou.core.mention.components.decorator;

import cn.qihuang02.callyou.api.MentionCandidate;
import cn.qihuang02.callyou.api.MentionContext;
import cn.qihuang02.callyou.api.MentionView;
import cn.qihuang02.callyou.api.components.InteractionDecorator;
import cn.qihuang02.callyou.registry.BuiltInCallYouRegistries;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record HoverEntityDecorator(ResourceLocation entityType, UUID uuid,
                                   Component name) implements InteractionDecorator {
    public static final MapCodec<HoverEntityDecorator> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("type").forGetter(HoverEntityDecorator::entityType),
            UUIDUtil.CODEC.fieldOf("uuid").forGetter(HoverEntityDecorator::uuid),
            ComponentSerialization.CODEC.optionalFieldOf("name", Component.empty()).forGetter(HoverEntityDecorator::name)
    ).apply(instance, HoverEntityDecorator::new));

    @Override
    public @NotNull InteractionDecoratorType type() {
        return BuiltInCallYouRegistries.HOVER_ENTITY_DECORATOR_TYPE.get();
    }

    @Override
    public @NotNull Component decorate(
            @NotNull Component original,
            @NotNull MentionContext context,
            @NotNull MentionCandidate candidate,
            @NotNull MentionView view
    ) {
        var entityType = BuiltInRegistries.ENTITY_TYPE.get(this.entityType);
        if (entityType == null) {
            return original;
        }
        HoverEvent.EntityTooltipInfo info = new HoverEvent.EntityTooltipInfo(entityType, this.uuid, this.name);
        HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_ENTITY, info);
        return original.copy().withStyle(style -> style.withHoverEvent(hoverEvent));
    }
}
