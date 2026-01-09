package cn.qihuang02.callyou.core.components.target;

import cn.qihuang02.callyou.api.MentionContext;
import cn.qihuang02.callyou.api.components.TargetProvider;
import cn.qihuang02.callyou.registry.BuiltInCallYouRegistries;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record DimensionTargetProvider(@Nullable ResourceLocation dimensionId) implements TargetProvider {

    public static final MapCodec<DimensionTargetProvider> MAP_CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    ResourceLocation.CODEC.optionalFieldOf("dimension").forGetter(p -> Optional.ofNullable(p.dimensionId))
            ).apply(instance, opt -> new DimensionTargetProvider(opt.orElse(null)))
    );

    public static @NotNull List<ServerPlayer> collectTargets(
            @NotNull MentionContext context,
            @Nullable ResourceLocation dimensionId
    ) {
        ServerPlayer sender = context.sender();
        MinecraftServer server = context.server();

        if (dimensionId == null) {
            ServerLevel level = context.level();
            List<ServerPlayer> result = new ArrayList<>();
            for (ServerPlayer p : level.players()) {
                if (p != sender) {
                    result.add(p);
                }
            }
            return result;
        }

        List<ServerPlayer> result = new ArrayList<>();
        for (ServerLevel level : server.getAllLevels()) {
            if (level.dimension().location().equals(dimensionId)) {
                for (ServerPlayer p : level.players()) {
                    if (p != sender) {
                        result.add(p);
                    }
                }
                break;
            }
        }
        return result;
    }

    @Override
    public @NotNull TargetProviderType type() {
        return BuiltInCallYouRegistries.DIMENSION_TARGET_TYPE.get();
    }

    @Override
    public @NotNull List<ServerPlayer> getTargets(@NotNull MentionContext context) {
        return collectTargets(context, this.dimensionId);
    }
}
