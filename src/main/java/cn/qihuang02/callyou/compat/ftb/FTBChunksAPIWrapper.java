package cn.qihuang02.callyou.compat.ftb;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

public class FTBChunksAPIWrapper {
    public static final String MOD_ID = "ftbchunks";
    public static final String TRANSIENT_WAYPOINT_COMMAND = "/callyou_ftb_transient_waypoint";

    public static boolean isLoaded() {
        return ModList.get().isLoaded(MOD_ID);
    }

    public static @NotNull String buildTransientWaypointCommand(
            @NotNull ResourceKey<Level> dimension,
            @NotNull BlockPos pos,
            String waypointName
    ) {
        String safeName = waypointName == null ? "" : waypointName;

        String encodedName = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(safeName.getBytes(StandardCharsets.UTF_8));

        return String.join(
                " ",
                TRANSIENT_WAYPOINT_COMMAND,
                dimension.location().toString(),
                Integer.toString(pos.getX()),
                Integer.toString(pos.getY()),
                Integer.toString(pos.getZ()),
                encodedName
        );
    }

    public static Optional<String> handleTransientWaypointCommand(@NotNull String rawCommand) {
        if (!rawCommand.startsWith(TRANSIENT_WAYPOINT_COMMAND)) {
            return Optional.empty();
        }

        String[] parts = rawCommand.substring(TRANSIENT_WAYPOINT_COMMAND.length()).trim().split("\\s+");
        if (parts.length < 5) {
            return Optional.empty();
        }

        ResourceLocation dimensionId = ResourceLocation.tryParse(parts[0]);
        if (dimensionId == null) {
            return Optional.empty();
        }

        int x;
        int y;
        int z;

        try {
            x = Integer.parseInt(parts[1]);
            y = Integer.parseInt(parts[2]);
            z = Integer.parseInt(parts[3]);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }

        String waypointName;
        try {
            waypointName = new String(Base64.getUrlDecoder().decode(parts[4]), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }

        return addTransientWaypointAt(
                ResourceKey.create(Registries.DIMENSION, dimensionId),
                new BlockPos(x, y, z),
                waypointName
        );
    }

    public static Optional<String> addTransientWaypointAt(
            ResourceKey<Level> dimension,
            BlockPos pos,
            String waypointName
    ) {
        if (!isLoaded() || FMLEnvironment.dist != Dist.CLIENT) {
            return Optional.empty();
        }

        String finalName = waypointName == null ? "" : waypointName.trim();
        if (finalName.isEmpty()) {
            return Optional.empty();
        }

        try {
            Class<?> apiClass = Class.forName("dev.ftb.mods.ftbchunks.api.FTBChunksAPI");
            Object clientApi = apiClass.getMethod("clientApi").invoke(null);
            Class<?> clientApiClass = Class.forName("dev.ftb.mods.ftbchunks.api.client.FTBChunksClientAPI");
            Object managerOptional = dimension == null
                    ? clientApiClass.getMethod("getWaypointManager").invoke(clientApi)
                    : clientApiClass.getMethod("getWaypointManager", ResourceKey.class).invoke(clientApi, dimension);
            Optional<?> manager = (Optional<?>) managerOptional;
            if (manager.isEmpty()) {
                Optional<?> fallback = (Optional<?>) clientApiClass.getMethod("getWaypointManager").invoke(clientApi);
                manager = fallback;
            }
            if (manager.isEmpty()) {
                return Optional.empty();
            }

            Object waypointManager = manager.get();
            Object waypoint = waypointManager.getClass()
                    .getMethod("addTransientWaypointAt", BlockPos.class, String.class)
                    .invoke(waypointManager, pos, finalName);
            if (waypoint == null) {
                return Optional.empty();
            }
            return Optional.of(finalName);
        } catch (ReflectiveOperationException | RuntimeException e) {
            return Optional.empty();
        }
    }
}
