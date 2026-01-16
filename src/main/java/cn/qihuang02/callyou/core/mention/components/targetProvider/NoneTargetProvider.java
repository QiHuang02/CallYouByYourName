package cn.qihuang02.callyou.core.mention.components.targetProvider;

import cn.qihuang02.callyou.api.MentionContext;
import cn.qihuang02.callyou.api.components.TargetProvider;
import cn.qihuang02.callyou.api.TargetCollection;
import cn.qihuang02.callyou.registry.BuiltInCallYouRegistries;
import com.mojang.serialization.MapCodec;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public enum NoneTargetProvider implements TargetProvider {
    INSTANCE;

    public static final MapCodec<NoneTargetProvider> MAP_CODEC = MapCodec.unit(INSTANCE);

    @Override
    public @NotNull TargetProviderType type() {
        return BuiltInCallYouRegistries.NONE_TARGET_TYPE.get();
    }

    @Contract(pure = true)
    @Override
    public @NotNull TargetCollection resolveTargets(MentionContext context) {
        return TargetCollection.empty();
    }
}
