package cn.qihuang02.cyyn.util;

import cn.qihuang02.cyyn.client.chat.ClientMentionGroupTokens;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class MentionCandidateProvider {
    private final Minecraft minecraft;

    public MentionCandidateProvider(@NotNull Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    private static @NotNull List<String> sortAndDeduplicate(@NotNull Collection<String> values) {
        if (values.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, String> deduplicated = new LinkedHashMap<>();
        for (String value : values) {
            if (value == null) {
                continue;
            }

            String trimmed = value.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            deduplicated.putIfAbsent(trimmed.toLowerCase(Locale.ROOT), trimmed);
        }

        List<String> sorted = new ArrayList<>(deduplicated.values());
        sorted.sort(String.CASE_INSENSITIVE_ORDER);
        return sorted;
    }

    public @NotNull Set<String> getGroupTokens(@NotNull Collection<String> tokens, @Nullable LocalPlayer player) {
        Set<String> normalized = new LinkedHashSet<>();
        for (String token : tokens) {
            if (token == null || token.isEmpty()) {
                continue;
            }
            normalized.add(token.toLowerCase(Locale.ROOT));
        }
        normalized.add("here");
        normalized.add("near");
        if (player != null && !player.getMainHandItem().isEmpty()) {
            normalized.add("item");
        }
        return normalized;
    }

    public @NotNull Set<String> getGroupTokens(@Nullable LocalPlayer player) {
        return getGroupTokens(ClientMentionGroupTokens.getTokens(), player);
    }

    public @NotNull Set<String> getGroupTokens() {
        return getGroupTokens(this.minecraft.player);
    }

    public @NotNull Set<String> getPlayerTokens(@Nullable ClientPacketListener connection, @Nullable LocalPlayer localPlayer) {
        if (connection == null) {
            return Collections.emptySet();
        }

        Set<String> names = new LinkedHashSet<>();
        for (PlayerInfo info : connection.getOnlinePlayers()) {
            GameProfile profile = info.getProfile();
            if (profile == null) {
                continue;
            }

            if (localPlayer != null && profile.getId() != null && profile.getId().equals(localPlayer.getUUID())) {
                continue;
            }

            String name = profile.getName();
            if (name != null && !name.isEmpty()) {
                names.add(name.toLowerCase(Locale.ROOT));
            }
        }
        return names;
    }

    public @NotNull Set<String> getPlayerTokens() {
        return getPlayerTokens(this.minecraft.getConnection(), this.minecraft.player);
    }

    public @NotNull List<String> getGroupCandidates(@Nullable LocalPlayer player) {
        List<String> candidates = new ArrayList<>(ClientMentionGroupTokens.getTokens());
        candidates.add("here");
        candidates.add("near");
        if (player != null && !player.getMainHandItem().isEmpty()) {
            candidates.add("item");
        }
        return sortAndDeduplicate(candidates);
    }

    public @NotNull List<String> getGroupCandidates() {
        return getGroupCandidates(this.minecraft.player);
    }

    public @NotNull List<String> getPlayerCandidates(@Nullable ClientPacketListener connection, @Nullable LocalPlayer localPlayer) {
        if (connection == null) {
            return Collections.emptyList();
        }

        List<String> names = new ArrayList<>();
        for (PlayerInfo info : connection.getOnlinePlayers()) {
            GameProfile profile = info.getProfile();
            if (profile == null) {
                continue;
            }

            if (localPlayer != null && profile.getId() != null && profile.getId().equals(localPlayer.getUUID())) {
                continue;
            }

            String name = profile.getName();
            if (name != null && !name.isEmpty()) {
                names.add(name);
            }
        }
        return sortAndDeduplicate(names);
    }

    public @NotNull List<String> getPlayerCandidates() {
        return getPlayerCandidates(this.minecraft.getConnection(), this.minecraft.player);
    }
}
