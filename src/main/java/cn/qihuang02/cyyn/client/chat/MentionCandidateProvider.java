package cn.qihuang02.cyyn.client.chat;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class MentionCandidateProvider {
    private static final String ITEM_FUNCTION_NAME = "item";
    private final Minecraft minecraft;
    private final ClientMentionContext mentionContext;

    public MentionCandidateProvider(@NotNull Minecraft minecraft) {
        this.minecraft = minecraft;
        this.mentionContext = ClientMentionContext.getInstance();
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

    private static @NotNull Predicate<ClientMentionContext.FunctionEntry> functionFilter(@Nullable LocalPlayer player) {
        boolean hasItemInHand = player != null && !player.getMainHandItem().isEmpty();
        if (hasItemInHand) {
            return entry -> true;
        }
        return entry -> !ITEM_FUNCTION_NAME.equals(entry.normalizedName());
    }

    private @NotNull List<String> collectKeywordDirectory(@Nullable LocalPlayer player,
                                                          @Nullable Consumer<? super List<String>> conditionalAppender) {
        ClientMentionContext.KeywordDirectory directory = this.mentionContext.keywordDirectory();
        return directory.collect(functionFilter(player), conditionalAppender);
    }

    private @NotNull Set<String> normalizeKeywords(@NotNull Collection<String> values) {
        if (values.isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> normalized = new LinkedHashSet<>();

        for (String value : values) {
            if (value == null || value.isEmpty()) {
                continue;
            }
            normalized.add(value.toLowerCase(Locale.ROOT));
        }
        return normalized;
    }

    public @NotNull Set<String> getGroupNames(@NotNull Collection<String> names, @Nullable LocalPlayer player) {
        List<String> keywords = collectKeywordDirectory(player, values -> values.addAll(names));
        return normalizeKeywords(keywords);
    }

    public @NotNull Set<String> getGroupNames(@Nullable LocalPlayer player) {
        return getGroupNames(ClientMentionGroupNames.getNames(), player);
    }

    public @NotNull Set<String> getGroupNames() {
        return getGroupNames(this.minecraft.player);
    }

    public @NotNull Set<String> getPlayerNames(@Nullable ClientPacketListener connection, @Nullable LocalPlayer localPlayer) {
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

    public @NotNull Set<String> getPlayerNames() {
        return getPlayerNames(this.minecraft.getConnection(), this.minecraft.player);
    }

    public @NotNull List<String> getGroupCandidates(@Nullable LocalPlayer player) {
        List<String> keywords = collectKeywordDirectory(player, null);
        return sortAndDeduplicate(keywords);
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
