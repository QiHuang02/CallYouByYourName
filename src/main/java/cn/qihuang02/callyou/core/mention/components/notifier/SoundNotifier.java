package cn.qihuang02.callyou.core.mention.components.notifier;

import cn.qihuang02.callyou.api.MentionCandidate;
import cn.qihuang02.callyou.api.MentionContext;
import cn.qihuang02.callyou.api.components.Notifier;
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

public record SoundNotifier(SoundEvent sound, float volume, float pitch) implements Notifier {
    public static final MapCodec<SoundNotifier> MAP_CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    BuiltInRegistries.SOUND_EVENT.byNameCodec()
                            .fieldOf("sound")
                            .forGetter(SoundNotifier::sound),
                    Codec.FLOAT
                            .optionalFieldOf("volume", 1.0f)
                            .forGetter(SoundNotifier::volume),
                    Codec.FLOAT
                            .optionalFieldOf("pitch", 1.0f)
                            .forGetter(SoundNotifier::pitch)
            ).apply(instance, SoundNotifier::new));

    @Contract(pure = true)
    @Override
    public @NotNull NotifierType type() {
        return BuiltInCallYouRegistries.SOUND_TYPE.get();
    }

    @Override
    public void apply(
            @NotNull MentionContext context,
            @NotNull MentionCandidate candidate,
            @NotNull List<ServerPlayer> targets
    ) {
        for (ServerPlayer player : targets) {
            player.playNotifySound(sound, SoundSource.PLAYERS, volume, pitch);
        }
    }
}
