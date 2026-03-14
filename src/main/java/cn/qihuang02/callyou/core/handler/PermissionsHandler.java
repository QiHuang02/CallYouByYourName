package cn.qihuang02.callyou.core.handler;

import cn.qihuang02.callyou.CallYouByYourName;
import cn.qihuang02.callyou.api.MentionType;
import cn.qihuang02.callyou.registry.CallYouMentionRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.server.permission.PermissionAPI;
import net.minecraftforge.server.permission.events.PermissionGatherEvent;
import net.minecraftforge.server.permission.nodes.PermissionDynamicContext;
import net.minecraftforge.server.permission.nodes.PermissionNode;
import net.minecraftforge.server.permission.nodes.PermissionTypes;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PermissionsHandler {

    public static final PermissionNode<Boolean> USE_MENTION =
            new PermissionNode<>(
                    CallYouByYourName.MODID,
                    "mention.use",
                    PermissionTypes.BOOLEAN,
                    (ServerPlayer player, UUID uuid, PermissionDynamicContext<?>... ctx) -> true
            );
    public static final PermissionNode<Boolean> USE_MASS_MENTION =
            new PermissionNode<>(
                    CallYouByYourName.MODID,
                    "mention.mass",
                    PermissionTypes.BOOLEAN,
                    (ServerPlayer player, UUID uuid, PermissionDynamicContext<?>... ctx) ->
                            player != null && player.hasPermissions(2)
            );
    private static final Map<ResourceLocation, PermissionNode<Boolean>> MENTION_PERMISSIONS = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onGatherPermissionNodes(PermissionGatherEvent.@NotNull Nodes event) {
        event.addNodes(USE_MENTION, USE_MASS_MENTION);

        refreshMentionPermissions(CallYouMentionRegistries.mentionTypes());

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

    public static void refreshMentionPermissions(@NotNull Map<ResourceLocation, MentionType> mentionTypes) {
        for (ResourceLocation id : mentionTypes.keySet()) {
            getMentionPermissionNode(id);
        }
    }

    private static @NotNull PermissionNode<Boolean> getMentionPermissionNode(@NotNull ResourceLocation mentionID) {
        return MENTION_PERMISSIONS.computeIfAbsent(mentionID, id -> new PermissionNode<>(
                id.getNamespace(),
                "mention." + id.getPath().replace("/", "."),
                PermissionTypes.BOOLEAN,
                (ServerPlayer player, UUID uuid, PermissionDynamicContext<?>... ctx) -> true
        ));
    }
}
