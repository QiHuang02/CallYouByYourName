package cn.qihuang02.callyou.core.components.target;

import cn.qihuang02.callyou.api.MentionContext;
import cn.qihuang02.callyou.api.TargetProvider;
import cn.qihuang02.callyou.registry.BuiltInCallYouRegistries;
import com.mojang.serialization.MapCodec;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.List;

public enum NoneTargetProvider implements TargetProvider {
    INSTANCE;

    public static final MapCodec<NoneTargetProvider> MAP_CODEC = MapCodec.unit(INSTANCE);

    @Override
    public @NotNull TargetProviderType type() {
        return BuiltInCallYouRegistries.NONE_TARGET_TYPE.get();
    }

    @Contract(pure = true)
    @Override
    public @NotNull @Unmodifiable List<ServerPlayer> getTargets(MentionContext context) {
        return Collections.emptyList();
    }
}
