package cn.qihuang02.callyou.integration.ftbteams;

import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class FTBTeamsAPIWrapper {
    public static final String MOD_ID = "ftbteams";

    public static boolean isLoaded() {
        return !ModList.get().isLoaded(MOD_ID);
    }

    @NotNull
    public static List<UUID> getTeamMembers(ServerPlayer player) {
        if (isLoaded()) {
            return Collections.emptyList();
        }

        try {
            return FTBTeamsAPI.api().getManager().getTeamForPlayer(player)
                    .map(team -> (List<UUID>) new ArrayList<>(team.getMembers()))
                    .orElse(Collections.emptyList());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
