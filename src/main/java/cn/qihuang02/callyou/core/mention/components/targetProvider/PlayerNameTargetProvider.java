package cn.qihuang02.callyou.core.mention.components.targetProvider;

import cn.qihuang02.callyou.api.MentionCandidate;
import cn.qihuang02.callyou.api.MentionContext;
import cn.qihuang02.callyou.api.components.TargetProvider;
import cn.qihuang02.callyou.core.handler.OnlinePlayersHandler;
import cn.qihuang02.callyou.registry.BuiltInCallYouRegistries;
import cn.qihuang02.callyou.util.OfflinePlayerList;
import cn.qihuang02.callyou.util.OnlinePlayerList;
import com.mojang.serialization.MapCodec;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class PlayerNameTargetProvider implements TargetProvider {
    public static final MapCodec<PlayerNameTargetProvider> MAP_CODEC =
            MapCodec.unit(PlayerNameTargetProvider::new);

    @Override
    public @NotNull TargetProviderType type() {
        return BuiltInCallYouRegistries.PLAYER_NAME_TYPE.get();
    }

    @Override
    public void resolveTargets(@NotNull MentionContext context, @NotNull MentionCandidate candidate) {
        String targetName = candidate.key();
        if (targetName.isEmpty()) {
            return;
        }

        ServerPlayer sender = context.sender();
        MinecraftServer server = context.server();
        if (server == null) {
            return;
        }

        OnlinePlayerList onlinePlayerList = OnlinePlayersHandler.getOnlinePlayers();
        UUID targetID = onlinePlayerList.findPlayerByExactName(server, targetName);
        if (targetID != null) {
            ServerPlayer target = server.getPlayerList().getPlayer(targetID);
            if (target != null && !target.equals(sender)) {
                candidate.addTarget(target.getUUID());
                return;
            }
        }

        OfflinePlayerList offlinePlayerList = OnlinePlayersHandler.getOfflinePlayers();
        UUID targetId = offlinePlayerList.findPlayerByExactName(server, targetName);
        if (targetId == null || targetId.equals(context.senderId())) {
            return;
        }

        if (server.getPlayerList().getPlayer(targetId) != null) {
            return;
        }

        candidate.addTarget(targetId);
    }
}
