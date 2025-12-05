package cn.qihuang02.callyou.core;

import cn.qihuang02.callyou.CallYouByYourName;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent;
import net.neoforged.neoforge.server.permission.nodes.PermissionDynamicContext;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import net.neoforged.neoforge.server.permission.nodes.PermissionTypes;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(modid = CallYouByYourName.MODID)
public final class CallYouPermissions {

    public static final PermissionNode<Boolean> USE_MENTION =
            new PermissionNode<>(
                    ResourceLocation.fromNamespaceAndPath(CallYouByYourName.MODID, "mention.use"),
                    PermissionTypes.BOOLEAN,
                    (ServerPlayer player, java.util.UUID uuid, PermissionDynamicContext<?>... ctx) -> true
            );

    public static final PermissionNode<Boolean> USE_MASS_MENTION =
            new PermissionNode<>(
                    ResourceLocation.fromNamespaceAndPath(CallYouByYourName.MODID, "mention.mass"),
                    PermissionTypes.BOOLEAN,
                    (ServerPlayer player, java.util.UUID uuid, PermissionDynamicContext<?>... ctx) ->
                            player != null && player.hasPermissions(2)
            );

    private CallYouPermissions() {}

    @SubscribeEvent
    public static void onGatherPermissionNodes(PermissionGatherEvent.@NotNull Nodes event) {
        event.addNodes(USE_MENTION, USE_MASS_MENTION);
    }

    public static boolean canUseMention(ServerPlayer player) {
        return PermissionAPI.getPermission(player, USE_MENTION);
    }

    public static boolean canUseMassMention(ServerPlayer player) {
        return PermissionAPI.getPermission(player, USE_MASS_MENTION);
    }

    public static boolean checkLogicalPermission(ServerPlayer player, String permission) {
        return true;
    }
}
