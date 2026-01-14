package cn.qihuang02.callyou.core.mention.components.targetProvider;

import cn.qihuang02.callyou.api.MentionContext;
import cn.qihuang02.callyou.api.components.TargetProvider;
import cn.qihuang02.callyou.core.handler.OnlinePlayersHandler;
import cn.qihuang02.callyou.registry.BuiltInCallYouRegistries;
import cn.qihuang02.callyou.util.OnlinePlayerList;
import com.mojang.serialization.MapCodec;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.UUID;

public final class PlayerNameTargetProvider implements TargetProvider {
    public static final MapCodec<PlayerNameTargetProvider> MAP_CODEC =
            MapCodec.unit(PlayerNameTargetProvider::new);

    @Override
    public @NotNull TargetProviderType type() {
        return BuiltInCallYouRegistries.PLAYER_NAME_TYPE.get();
    }

    @Override
    public @NotNull @Unmodifiable List<ServerPlayer> getTargets(@NotNull MentionContext context) {
        String targetName = context.mentionKey();
        if (targetName == null || targetName.isEmpty()) {
            return List.of();
        }

        ServerPlayer sender = context.sender();
        MinecraftServer server = context.server();
        if (server == null) {
            return List.of();
        }

        OnlinePlayerList onlinePlayerList = OnlinePlayersHandler.getOnlinePlayers();
        UUID targetID = onlinePlayerList.findPlayerByExactName(server, targetName);
        if (targetID == null) {
            return List.of();
        }

        ServerPlayer target = server.getPlayerList().getPlayer(targetID);
        if (target == null || target.equals(sender)) {
            return List.of();
        }

        return List.of(target);
    }
}
