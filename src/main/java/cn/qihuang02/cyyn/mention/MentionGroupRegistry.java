package cn.qihuang02.cyyn.mention;

import cn.qihuang02.cyyn.util.MentionTextUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.stream.Stream;

public final class MentionGroupRegistry {
    private static final ConcurrentMap<String, MentionGroup> GROUPS = new ConcurrentHashMap<>();
    private static final CopyOnWriteArrayList<Consumer<? super Collection<String>>> LISTENERS = new CopyOnWriteArrayList<>();

    public static void register(@NotNull MentionGroup mentionGroup) {
        Objects.requireNonNull(mentionGroup, "mentionGroup");
        String token = Objects.requireNonNull(mentionGroup.token(), "mentionGroup token");
        GROUPS.put(normalizeToken(token), mentionGroup);
        notifyListeners();
    }

    public static void unregister(@NotNull String token) {
        GROUPS.remove(normalizeToken(Objects.requireNonNull(token, "token")));
        notifyListeners();
    }

    public static @NotNull Optional<MentionGroup> find(@NotNull String token) {
        return Optional.ofNullable(GROUPS.get(normalizeToken(Objects.requireNonNull(token, "token"))));
    }

    public static @NotNull Stream<MentionGroup> stream() {
        return GROUPS.values().stream();
    }

    public static @NotNull @Unmodifiable List<String> tokens() {
        return GROUPS.values().stream()
                .map(MentionGroup::token)
                .filter(Objects::nonNull)
                .toList();
    }

    public static void addListener(@NotNull Consumer<? super Collection<String>> listener) {
        Consumer<? super Collection<String>> consumer = Objects.requireNonNull(listener, "listener");
        LISTENERS.add(consumer);
        consumer.accept(Collections.unmodifiableList(List.copyOf(tokens())));
    }

    public static void removeListener(@NotNull Consumer<? super Collection<String>> listener) {
        LISTENERS.remove(Objects.requireNonNull(listener, "listener"));
    }

    private static void notifyListeners() {
        if (LISTENERS.isEmpty()) {
            return;
        }
        List<String> snapshot = List.copyOf(tokens());
        for (Consumer<? super Collection<String>> listener : LISTENERS) {
            listener.accept(snapshot);
        }
    }

    private static @NotNull String normalizeToken(@NotNull String token) {
        String normalized = MentionTextUtils.normalizeToken(Objects.requireNonNull(token, "token"));
        return normalized.toLowerCase(Locale.ROOT);
    }
}
