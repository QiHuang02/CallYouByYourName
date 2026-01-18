package cn.qihuang02.callyou.core.mention.components.targetProvider;

import cn.qihuang02.callyou.api.MentionCandidate;
import cn.qihuang02.callyou.api.MentionContext;
import cn.qihuang02.callyou.api.components.TargetProvider;
import cn.qihuang02.callyou.registry.BuiltInCallYouRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record RadiusTargetProvider(double range) implements TargetProvider {
    public static final MapCodec<RadiusTargetProvider> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.DOUBLE.fieldOf("range").forGetter(RadiusTargetProvider::range)
    ).apply(instance, RadiusTargetProvider::new));

    @Override
    public @NotNull TargetProviderType type() {
        return BuiltInCallYouRegistries.RADIUS_TYPE.get();
    }

    @Override
    public void resolveTargets(@NotNull MentionContext context, @NotNull MentionCandidate candidate) {
        ServerPlayer sender = context.sender();
        ServerLevel level = context.level();

        List<ServerPlayer> result = new ArrayList<>();
        for (ServerPlayer player : level.players()) {
            if (player == sender) continue;
            if (player.distanceTo(sender) <= range) {
                result.add(player);
            }
        }
        for (ServerPlayer player : result) {
            candidate.addTarget(player.getUUID());
        }
    }
}
