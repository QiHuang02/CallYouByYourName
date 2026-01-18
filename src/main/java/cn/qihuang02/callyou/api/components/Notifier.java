package cn.qihuang02.callyou.api.components;

import cn.qihuang02.callyou.api.MentionCandidate;
import cn.qihuang02.callyou.api.MentionContext;
import cn.qihuang02.callyou.registry.CallYouRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface Notifier extends IDispatchedComponent<Notifier, Notifier.NotifierType> {
    Codec<Notifier> CODEC = IDispatchedComponent.codec(CallYouRegistries.NOTIFICATION_RULE_TYPES);

    NotifierType type();

    void apply(@NotNull MentionContext context, @NotNull MentionCandidate candidate, @NotNull List<ServerPlayer> targets);

    record NotifierType(MapCodec<? extends Notifier> mapCodec) implements IDispatchedComponent.Type<Notifier> {
    }
}
