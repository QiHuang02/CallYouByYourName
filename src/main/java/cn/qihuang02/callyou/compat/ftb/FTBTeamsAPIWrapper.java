package cn.qihuang02.callyou.compat.ftb;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class FTBTeamsAPIWrapper {
    public static final String MOD_ID = "ftbteams";

    public static boolean isLoaded() {
        return ModList.get().isLoaded(MOD_ID);
    }

    @NotNull
    public static List<UUID> getTeamMembers(ServerPlayer player) {
        if (!isLoaded()) {
            return Collections.emptyList();
        }

        try {
            Class<?> apiClass = Class.forName("dev.ftb.mods.ftbteams.api.FTBTeamsAPI");
            Object api = apiClass.getMethod("api").invoke(null);
            Object manager = api.getClass().getMethod("getManager").invoke(api);
            Optional<?> teamOptional = (Optional<?>) manager.getClass()
                    .getMethod("getTeamForPlayer", ServerPlayer.class)
                    .invoke(manager, player);
            if (teamOptional.isEmpty()) {
                return Collections.emptyList();
            }

            Object team = teamOptional.get();
            Object members = team.getClass().getMethod("getMembers").invoke(team);
            List<UUID> results = new ArrayList<>();
            if (members instanceof Iterable<?> iterable) {
                for (Object member : iterable) {
                    if (member instanceof UUID uuid) {
                        results.add(uuid);
                    }
                }
            }
            return results;
        } catch (ReflectiveOperationException | RuntimeException e) {
            return Collections.emptyList();
        }
    }
}
