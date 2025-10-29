package cn.qihuang02.cyyn.client.chat;

import cn.qihuang02.cyyn.api.mention.MentionRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Provides cached mention related data for client side features such as highlighting and
 * auto-completion. This centralizes the logic that was previously spread between
 * {@link MentionHighlighter}, {@link MentionCandidateProvider} and
 * {@link ClientMentionGroupNames} so that styles and keyword directories can be reused without
 * redundant allocations.
 */
public final class ClientMentionContext {
    private static final ClientMentionContext INSTANCE = new ClientMentionContext();

    private final Object lock = new Object();

    private int cachedGroupRevision = -1;
    private boolean registryDirty = true;
    private int registryVersion;
    private int cachedRegistryVersion = -1;

    private Set<String> normalizedGroupNames = Set.of();
    private Map<String, Style> groupStyles = Map.of();
    private Map<String, Style> functionStyles = Map.of();
    private List<String> groupDirectory = List.of();
    private List<FunctionEntry> functionDirectory = List.of();

    private ClientMentionContext() {
    }

    public static @NotNull ClientMentionContext getInstance() {
        return INSTANCE;
    }

    public static @NotNull Style styleFor(@NotNull ChatFormatting color) {
        return Style.EMPTY.withColor(Objects.requireNonNull(color, "color"));
    }

    /**
     * Marks cached group derived data as dirty.
     */
    public void invalidateGroups() {
        synchronized (this.lock) {
            this.cachedGroupRevision = -1;
        }
    }

    /**
     * Marks cached registry derived data as dirty. Should be invoked if mention
     * groups or functions are registered/unregistered at runtime.
     */
    public void invalidateRegistry() {
        synchronized (this.lock) {
            this.registryDirty = true;
            this.registryVersion++;
        }
    }

    private void rebuildIfNeeded() {
        synchronized (this.lock) {
            int revision = ClientMentionGroupNames.getRevision();
            if (this.cachedGroupRevision != revision) {
                rebuildGroups();
                this.cachedGroupRevision = revision;
            }
            if (this.registryDirty || this.cachedRegistryVersion != this.registryVersion) {
                rebuildRegistry();
                this.registryDirty = false;
                this.cachedRegistryVersion = this.registryVersion;
            }
        }
    }

    private void rebuildGroups() {
        List<String> names = ClientMentionGroupNames.getNames();
        Set<String> normalized = new LinkedHashSet<>();
        for (String name : names) {
            if (name == null || name.isEmpty()) {
                continue;
            }
            normalized.add(name.toLowerCase(Locale.ROOT));
        }
        this.groupDirectory = List.copyOf(names);
        this.normalizedGroupNames = Set.copyOf(normalized);
    }

    private void rebuildRegistry() {
        Map<String, Style> groupStyles = new LinkedHashMap<>();
        MentionRegistry.streamGroups().forEach(group -> {
            String name = group.name();
            if (name == null || name.isEmpty()) {
                return;
            }
            groupStyles.put(name.toLowerCase(Locale.ROOT), styleFor(group.pointColor()));
        });

        Map<String, Style> functionStyles = new LinkedHashMap<>();
        List<FunctionEntry> functions = new ArrayList<>();
        MentionRegistry.streamFunctions().forEach(function -> {
            String name = function.name();
            if (name == null || name.isEmpty()) {
                return;
            }
            String normalized = name.toLowerCase(Locale.ROOT);
            Style style = styleFor(function.pointColor());
            functionStyles.put(normalized, style);
            functions.add(new FunctionEntry(name, normalized, style));
        });

        this.groupStyles = Map.copyOf(groupStyles);
        this.functionStyles = Map.copyOf(functionStyles);
        this.functionDirectory = List.copyOf(functions);
    }

    public @NotNull Set<String> getNormalizedGroupNames() {
        rebuildIfNeeded();
        synchronized (this.lock) {
            return this.normalizedGroupNames;
        }
    }

    public @NotNull Map<String, Style> getGroupStyles() {
        rebuildIfNeeded();
        synchronized (this.lock) {
            return this.groupStyles;
        }
    }

    public @NotNull Map<String, Style> getFunctionStyles() {
        rebuildIfNeeded();
        synchronized (this.lock) {
            return this.functionStyles;
        }
    }

    public @NotNull KeywordDirectory keywordDirectory() {
        rebuildIfNeeded();
        synchronized (this.lock) {
            return new KeywordDirectory(this.groupDirectory, this.functionDirectory);
        }
    }

    public int getRegistryVersion() {
        rebuildIfNeeded();
        synchronized (this.lock) {
            return this.registryVersion;
        }
    }

    public static final class KeywordDirectory {
        private final List<String> groups;
        private final List<FunctionEntry> functions;

        private KeywordDirectory(@NotNull List<String> groups, @NotNull List<FunctionEntry> functions) {
            this.groups = List.copyOf(groups);
            this.functions = List.copyOf(functions);
        }

        public @NotNull List<String> collect(@Nullable Predicate<? super FunctionEntry> functionFilter,
                                             @Nullable Consumer<? super List<String>> conditionalAppender) {
            List<String> values = new ArrayList<>(this.groups);
            Predicate<? super FunctionEntry> filter = functionFilter != null ? functionFilter : entry -> true;
            for (FunctionEntry entry : this.functions) {
                if (filter.test(entry)) {
                    values.add(entry.name());
                }
            }
            if (conditionalAppender != null) {
                conditionalAppender.accept(values);
            }
            return values;
        }

        public @NotNull List<FunctionEntry> functions() {
            return this.functions;
        }
    }

    public record FunctionEntry(@NotNull String name,
                                @NotNull String normalizedName,
                                @NotNull Style style) {
    }
}
