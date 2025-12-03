package cn.qihuang02.callyou.chat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class MentionCandidateProvider {
    private final Minecraft minecraft;

    public MentionCandidateProvider(@NotNull Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    public @NotNull List<String> getPlayerCandidates() {
        ClientPacketListener connection = minecraft.getConnection();
        LocalPlayer localPlayer = minecraft.player;
        if (connection == null || localPlayer == null) {
            return List.of();
        }

        String selfName = localPlayer.getGameProfile().getName();
        List<String> result = new ArrayList<>();

        for (PlayerInfo info : connection.getOnlinePlayers()) {
            String name = info.getProfile().getName();
            if (name == null || name.isEmpty()) continue;
            if (name.equals(selfName)) continue;
            result.add(name);
        }

        return result.stream()
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }
}
