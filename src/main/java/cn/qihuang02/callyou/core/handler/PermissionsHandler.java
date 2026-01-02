package cn.qihuang02.callyou.core.handler;

import cn.qihuang02.callyou.CallYouByYourName;
import cn.qihuang02.callyou.api.MentionType;
import cn.qihuang02.callyou.registry.CallYouMentionRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.server.permission.PermissionAPI;
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent;
import net.neoforged.neoforge.server.permission.nodes.PermissionDynamicContext;
import net.neoforged.neoforge.server.permission.nodes.PermissionNode;
import net.neoforged.neoforge.server.permission.nodes.PermissionTypes;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = CallYouByYourName.MODID)
public final class PermissionsHandler {

    private static final Map<ResourceLocation, PermissionNode<Boolean>> MENTION_PERMISSIONS = new ConcurrentHashMap<>();

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

    @SubscribeEvent
    public static void onGatherPermissionNodes(PermissionGatherEvent.@NotNull Nodes event) {
        event.addNodes(USE_MENTION, USE_MASS_MENTION);

        var server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            server.registryAccess()
                    .registry(CallYouMentionRegistries.MENTION_TYPE_REGISTRY_KEY)
                    .ifPresent(PermissionsHandler::refreshMentionPermissions);
        }

        for (PermissionNode<Boolean> node : MENTION_PERMISSIONS.values()) {
            event.addNodes(node);
        }
    }

    public static boolean canUseMention(ServerPlayer player) {
        return PermissionAPI.getPermission(player, USE_MENTION);
    }

    public static boolean canUseMassMention(ServerPlayer player) {
        return PermissionAPI.getPermission(player, USE_MASS_MENTION);
    }

    public static boolean canUseMentionType(@NotNull ServerPlayer player, @NotNull ResourceLocation mentionID) {
        PermissionNode<Boolean> node = getMentionPermissionNode(mentionID);
        return PermissionAPI.getPermission(player, node);
    }

    public static void refreshMentionPermissions(@NotNull Registry<MentionType> registry) {
        for (var entry : registry.entrySet()) {
            getMentionPermissionNode(entry.getKey().location());
        }
    }

    private static @NotNull PermissionNode<Boolean> getMentionPermissionNode(@NotNull ResourceLocation mentionID) {
        return MENTION_PERMISSIONS.computeIfAbsent(mentionID, id -> new PermissionNode<>(
                buildMentionPermissionID(id),
                PermissionTypes.BOOLEAN,
                (ServerPlayer player, UUID uuid, PermissionDynamicContext<?>... ctx) -> true
        ));
    }

    private static @NotNull ResourceLocation buildMentionPermissionID(@NotNull ResourceLocation mentionID) {
        String path = "mention." + mentionID.getPath().replace("/", ".");
        return ResourceLocation.fromNamespaceAndPath(mentionID.getNamespace(), path);
    }
}
