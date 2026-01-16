package cn.qihuang02.callyou.core.mention.components.targetProvider;

import cn.qihuang02.callyou.api.MentionContext;
import cn.qihuang02.callyou.api.components.TargetProvider;
import cn.qihuang02.callyou.api.TargetCollection;
import cn.qihuang02.callyou.registry.BuiltInCallYouRegistries;
import com.mojang.serialization.MapCodec;
import org.jetbrains.annotations.NotNull;

public class SameDimensionTargetProvider implements TargetProvider {
    public static final MapCodec<SameDimensionTargetProvider> MAP_CODEC =
            MapCodec.unit(new SameDimensionTargetProvider());

    @Override
    public @NotNull TargetProviderType type() {
        return BuiltInCallYouRegistries.SAME_DIMENSION_TARGET_TYPE.get();
    }

    @Override
    public @NotNull TargetCollection resolveTargets(@NotNull MentionContext context) {
        // 复用 DimensionTargetProvider 的通用逻辑，传 null 表示“当前维度”
        return DimensionTargetProvider.collectTargets(context, null);
    }
}
