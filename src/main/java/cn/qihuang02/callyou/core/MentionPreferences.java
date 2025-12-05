package cn.qihuang02.callyou.core;

import cn.qihuang02.callyou.api.MentionRules;
import cn.qihuang02.callyou.api.MentionType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;
import java.util.stream.Collectors;

public final class MentionPreferences {

    public static final Codec<MentionPreferences> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.BOOL.optionalFieldOf("allow_mentions", true)
                            .forGetter(MentionPreferences::isAllowMentions),
                    Codec.BOOL.optionalFieldOf("allow_mass_mentions", true)
                            .forGetter(MentionPreferences::isAllowMassMentions),
                    Codec.STRING.listOf().optionalFieldOf("blocked_keys", List.of())
                            .forGetter(p -> new ArrayList<>(p.blockedKeys)),
                    Codec.STRING.listOf().optionalFieldOf("blocked_senders", List.of())
                            .forGetter(p -> p.blockedSenders.stream()
                                    .map(UUID::toString)
                                    .collect(Collectors.toList()))
            ).apply(instance, MentionPreferences::fromCodec)
    );
    final Set<UUID> blockedSenders = new HashSet<>();
    private final Set<String> blockedKeys = new HashSet<>();
    private boolean allowMentions = true;
    private boolean allowMassMentions = true;

    private static @NotNull MentionPreferences fromCodec(boolean allowMentions,
                                                         boolean allowMassMentions,
                                                         @NotNull List<String> blockedKeys,
                                                         List<String> blockedSenderStrings) {
        MentionPreferences prefs = new MentionPreferences();
        prefs.allowMentions = allowMentions;
        prefs.allowMassMentions = allowMassMentions;

        for (String key : blockedKeys) {
            prefs.blockMentionKey(key);
        }
        for (String s : blockedSenderStrings) {
            try {
                prefs.blockedSenders.add(UUID.fromString(s));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return prefs;
    }

    @Contract(pure = true)
    private static @NotNull String normalizeKey(@NotNull String key) {
        return key.toLowerCase(Locale.ROOT);
    }

    public boolean isAllowMentions() {
        return allowMentions;
    }

    public void setAllowMentions(boolean allowMentions) {
        this.allowMentions = allowMentions;
    }

    public boolean isAllowMassMentions() {
        return allowMassMentions;
    }

    public void setAllowMassMentions(boolean allowMassMentions) {
        this.allowMassMentions = allowMassMentions;
    }

    @Contract(pure = true)
    public @NotNull @UnmodifiableView Set<String> getBlockedKeys() {
        return Collections.unmodifiableSet(blockedKeys);
    }

    @Contract(pure = true)
    public @NotNull @UnmodifiableView Set<UUID> getBlockedSenders() {
        return Collections.unmodifiableSet(blockedSenders);
    }

    public void blockMentionKey(String rawKey) {
        if (rawKey == null || rawKey.isEmpty()) return;
        blockedKeys.add(normalizeKey(rawKey));
    }

    public void unblockMentionKey(String rawKey) {
        if (rawKey == null || rawKey.isEmpty()) return;
        blockedKeys.remove(normalizeKey(rawKey));
    }

    public void blockSender(UUID senderId) {
        if (senderId != null) {
            blockedSenders.add(senderId);
        }
    }

    public void unblockSender(UUID senderId) {
        if (senderId != null) {
            blockedSenders.remove(senderId);
        }
    }

    public void resetAll() {
        this.allowMentions = true;
        this.allowMassMentions = true;
        this.blockedKeys.clear();
        this.blockedSenders.clear();
    }

    public boolean isMentionAllowed(@NotNull MentionType type,
                                    @NotNull String key,
                                    @NotNull UUID senderId) {

        if (!allowMentions) {
            return false;
        }

        if (blockedSenders.contains(senderId)) {
            return false;
        }

        if (blockedKeys.contains(normalizeKey(key))) {
            return false;
        }

        MentionRules rules = type.rules();
        if (!allowMassMentions && rules != null && rules.isMass()) {
            return false;
        }

        return true;
    }
}
