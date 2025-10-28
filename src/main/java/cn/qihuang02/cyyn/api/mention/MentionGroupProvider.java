package cn.qihuang02.cyyn.api.mention;

/**
 * Service provider interface that allows other mods to contribute custom {@link MentionGroup MentionGroups}.
 * <p>
 * Implementations are discovered via Java's {@link java.util.ServiceLoader ServiceLoader} and invoked during
 * the mod bootstrap process.
 */
@FunctionalInterface
public interface MentionGroupProvider {
    /**
     * Register one or more mention groups with the {@link MentionGroupRegistry}.
     *
     * <p>Providers are executed on the main thread during mod initialization, so implementations should avoid
     * heavy work. Providers are expected to call {@link MentionGroupRegistry#register(MentionGroup)} for each
     * group they contribute.</p>
     */
    void registerMentionGroups();
}
