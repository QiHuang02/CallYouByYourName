package cn.qihuang02.callyou.core.impl.notify;

import cn.qihuang02.callyou.api.MentionContext;
import cn.qihuang02.callyou.api.NotificationRule;
import cn.qihuang02.callyou.api.NotificationRuleType;
import cn.qihuang02.callyou.registry.BuiltInCallYouRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record SoundNotificationRule(SoundEvent sound, float volume, float pitch) implements NotificationRule {
    public static final MapCodec<SoundNotificationRule> MAP_CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    BuiltInRegistries.SOUND_EVENT.byNameCodec()
                            .fieldOf("sound")
                            .forGetter(SoundNotificationRule::sound),
                    Codec.FLOAT
                            .optionalFieldOf("volume", 1.0f)
                            .forGetter(SoundNotificationRule::volume),
                    Codec.FLOAT
                            .optionalFieldOf("pitch", 1.0f)
                            .forGetter(SoundNotificationRule::pitch)
            ).apply(instance, SoundNotificationRule::new));

    @Contract(pure = true)
    @Override
    public @NotNull NotificationRuleType type() {
        return BuiltInCallYouRegistries.SOUND_TYPE.get();
    }

    @Override
    public void apply(MentionContext context, @NotNull List<ServerPlayer> targets) {
        for (ServerPlayer player : targets) {
            player.playNotifySound(sound, SoundSource.PLAYERS, volume, pitch);
        }
    }
}
