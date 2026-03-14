package cn.qihuang02.callyou.core.mention.components.targetProvider;

import cn.qihuang02.callyou.api.MentionCandidate;
import cn.qihuang02.callyou.api.MentionContext;
import cn.qihuang02.callyou.api.components.TargetProvider;
import cn.qihuang02.callyou.compat.ftb.FTBTeamsAPIWrapper;
import cn.qihuang02.callyou.registry.BuiltInCallYouRegistries;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FTBTeamTargetProvider implements TargetProvider {
    public static final MapCodec<FTBTeamTargetProvider> MAP_CODEC = MapCodec.unit(new FTBTeamTargetProvider());

    @Override
    public TargetProviderType type() {
        return BuiltInCallYouRegistries.FTB_TEAM_TARGET_TYPE;
    }

    @Override
    public void resolveTargets(@NotNull MentionContext context, @NotNull MentionCandidate candidate) {
        if (!FTBTeamsAPIWrapper.isLoaded()) {
            context.sender().sendSystemMessage(Component.translatable("message.callyou.ftbteams_missing"));
            return;
        }

        List<UUID> memberUUIDs = FTBTeamsAPIWrapper.getTeamMembers(context.sender());
        if (memberUUIDs.isEmpty()) {
            return;
        }

        List<UUID> targets = new ArrayList<>();
        UUID senderId = context.senderId();
        for (UUID uuid : memberUUIDs) {
            if (uuid.equals(senderId)) {
                continue;
            }
            targets.add(uuid);
        }

        if (targets.isEmpty()) {
            return;
        }
        for (UUID targetId : targets) {
            candidate.addTarget(targetId);
        }
    }
}
