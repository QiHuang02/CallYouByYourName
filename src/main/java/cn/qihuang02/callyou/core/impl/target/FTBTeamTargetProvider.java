package cn.qihuang02.callyou.core.impl.target;

import cn.qihuang02.callyou.api.MentionContext;
import cn.qihuang02.callyou.api.TargetProvider;
import cn.qihuang02.callyou.api.TargetProviderType;
import cn.qihuang02.callyou.integration.ftbteams.FTBTeamsAPIWrapper;
import cn.qihuang02.callyou.registry.BuiltInCallYouRegistries;
import com.mojang.serialization.MapCodec;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class FTBTeamTargetProvider implements TargetProvider {
    public static final MapCodec<FTBTeamTargetProvider> MAP_CODEC = MapCodec.unit(new FTBTeamTargetProvider());

    @Override
    public TargetProviderType type() {
        return BuiltInCallYouRegistries.FTB_TEAM_TARGET_TYPE.get();
    }

    @Override
    public List<ServerPlayer> getTargets(@NotNull MentionContext context) {
        ServerPlayer sender = context.sender();

        if (FTBTeamsAPIWrapper.isLoaded()) {
            return Collections.emptyList();
        }

        List<UUID> memberUUIDs = FTBTeamsAPIWrapper.getTeamMembers(sender);

        if (memberUUIDs.isEmpty()) {
            return Collections.emptyList();
        }

        List<ServerPlayer> targets = new ArrayList<>();
        for (UUID uuid : memberUUIDs) {
            if (uuid.equals(sender.getUUID())) {
                continue;
            }
            ServerPlayer target = sender.server.getPlayerList().getPlayer(uuid);
            if (target != null) {
                targets.add(target);
            }
        }

        return targets;
    }
}
