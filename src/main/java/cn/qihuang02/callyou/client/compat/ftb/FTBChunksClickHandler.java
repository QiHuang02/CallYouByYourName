package cn.qihuang02.callyou.client.compat.ftb;

import cn.qihuang02.callyou.compat.ftb.FTBChunksAPIWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ClickEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public final class FTBChunksClickHandler {
    private FTBChunksClickHandler() {
    }

    public static boolean handle(@Nullable ClickEvent clickEvent) {
        if (clickEvent == null || clickEvent.getAction() != ClickEvent.Action.RUN_COMMAND) {
            return false;
        }

        String value = clickEvent.getValue();
        if (value == null || value.isEmpty()) {
            return false;
        }

        return FTBChunksAPIWrapper.handleTransientWaypointCommand(value)
                .map(waypoint -> {
                    if (Minecraft.getInstance().player != null) {
                        Component msg = Component.translatable("message.callyou.spot.ftb.added", waypoint.getName());
                        Minecraft.getInstance().player.displayClientMessage(msg, true);
                    }
                    return true;
                })
                .orElse(false);
    }
}
