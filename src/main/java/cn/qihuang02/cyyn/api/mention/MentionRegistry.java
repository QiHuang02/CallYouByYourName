package cn.qihuang02.cyyn.api.mention;

import cn.qihuang02.cyyn.common.mention.MentionTextUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.stream.Stream;

public final class MentionRegistry {
    private static final ConcurrentMap<String, MentionGroup> GROUPS = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, MentionFunction> FUNCTIONS = new ConcurrentHashMap<>();
    private static final CopyOnWriteArrayList<String> GROUP_ORDER = new CopyOnWriteArrayList<>();
    private static final CopyOnWriteArrayList<String> FUNCTION_ORDER = new CopyOnWriteArrayList<>();
    private static final CopyOnWriteArrayList<Consumer<? super Collection<String>>> GROUP_LISTENERS = new CopyOnWriteArrayList<>();

    private MentionRegistry() {
    }

    public static void register(@NotNull MentionGroup mentionGroup) {
        Objects.requireNonNull(mentionGroup, "mentionGroup");
        String normalized = normalizeName(Objects.requireNonNull(mentionGroup.name(), "mentionGroup name"));
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Mention group name cannot be empty");
        }
        assertNameAvailable(normalized, GROUPS, FUNCTIONS, mentionGroup);

        MentionGroup previous = GROUPS.putIfAbsent(normalized, mentionGroup);
        if (previous != null && previous != mentionGroup) {
            throw new IllegalStateException("Mention group name already registered: " + normalized);
        }

        if (previous == null) {
            GROUP_ORDER.addIfAbsent(normalized);
            notifyGroupListeners();
        }
    }

    public static void unregisterGroup(@NotNull String name) {
        String normalized = normalizeName(Objects.requireNonNull(name, "name"));
        MentionGroup removed = GROUPS.remove(normalized);
        if (removed != null) {
            GROUP_ORDER.remove(normalized);
            notifyGroupListeners();
        }
    }

    public static void unregister(@NotNull MentionGroup mentionGroup) {
        Objects.requireNonNull(mentionGroup, "mentionGroup");
        unregisterGroup(mentionGroup.name());
    }

    public static @NotNull Optional<MentionGroup> findGroup(@NotNull String name) {
        return Optional.ofNullable(GROUPS.get(normalizeName(Objects.requireNonNull(name, "name"))));
    }

    public static @NotNull Stream<MentionGroup> streamGroups() {
        return GROUP_ORDER.stream()
                .map(GROUPS::get)
                .filter(Objects::nonNull);
    }

    public static @NotNull @Unmodifiable List<String> groupNames() {
        return streamGroups()
                .map(MentionGroup::name)
                .filter(name -> name != null && !name.isEmpty())
                .map(MentionTextUtils::normalizeToken)
                .map(name -> name.toLowerCase(Locale.ROOT))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    public static void addGroupListener(@NotNull Consumer<? super Collection<String>> listener) {
        Consumer<? super Collection<String>> consumer = Objects.requireNonNull(listener, "listener");
        GROUP_LISTENERS.add(consumer);
        consumer.accept(groupNames());
    }

    public static void removeGroupListener(@NotNull Consumer<? super Collection<String>> listener) {
        GROUP_LISTENERS.remove(Objects.requireNonNull(listener, "listener"));
    }

    public static void register(@NotNull MentionFunction mentionFunction) {
        Objects.requireNonNull(mentionFunction, "mentionFunction");
        String normalized = normalizeName(Objects.requireNonNull(mentionFunction.name(), "mentionFunction name"));
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Mention function name cannot be empty");
        }
        assertNameAvailable(normalized, FUNCTIONS, GROUPS, mentionFunction);

        MentionFunction previous = FUNCTIONS.putIfAbsent(normalized, mentionFunction);
        if (previous != null && previous != mentionFunction) {
            throw new IllegalStateException("Mention function name already registered: " + normalized);
        }

        if (previous == null) {
            FUNCTION_ORDER.addIfAbsent(normalized);
        }
    }

    public static void unregisterFunction(@NotNull String name) {
        String normalized = normalizeName(Objects.requireNonNull(name, "name"));
        MentionFunction removed = FUNCTIONS.remove(normalized);
        if (removed != null) {
            FUNCTION_ORDER.remove(normalized);
        }
    }

    public static void unregister(@NotNull MentionFunction mentionFunction) {
        Objects.requireNonNull(mentionFunction, "mentionFunction");
        unregisterFunction(mentionFunction.name());
    }

    public static @NotNull Optional<MentionFunction> findFunction(@NotNull String name) {
        return Optional.ofNullable(FUNCTIONS.get(normalizeName(Objects.requireNonNull(name, "name"))));
    }

    public static @NotNull Stream<MentionFunction> streamFunctions() {
        return FUNCTION_ORDER.stream()
                .map(FUNCTIONS::get)
                .filter(Objects::nonNull);
    }

    private static void notifyGroupListeners() {
        if (GROUP_LISTENERS.isEmpty()) {
            return;
        }
        List<String> snapshot = groupNames();
        for (Consumer<? super Collection<String>> listener : GROUP_LISTENERS) {
            listener.accept(snapshot);
        }
    }

    private static void assertNameAvailable(@NotNull String normalized,
                                            @NotNull ConcurrentMap<String, ? extends Mention> primary,
                                            @NotNull ConcurrentMap<String, ? extends Mention> secondary,
                                            @NotNull Mention mention) {
        Mention existing = primary.get(normalized);
        if (existing != null && existing != mention) {
            throw new IllegalStateException("Name already registered: " + normalized);
        }
        Mention conflicting = secondary.get(normalized);
        if (conflicting != null && conflicting != mention) {
            throw new IllegalStateException("Name already registered in other registry: " + normalized);
        }
    }

    private static @NotNull String normalizeName(@NotNull String name) {
        return MentionTextUtils.normalizeToken(Objects.requireNonNull(name, "name")).toLowerCase(Locale.ROOT);
    }
}
