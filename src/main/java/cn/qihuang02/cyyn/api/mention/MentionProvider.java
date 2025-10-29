package cn.qihuang02.cyyn.api.mention;

/**
 * Service provider interface that allows other mods to contribute custom {@link MentionGroup mention groups}
 * and {@link MentionFunction mention functions}.
 * <p>
 * Implementations should be supplied to {@link cn.qihuang02.cyyn.server.mention.MentionRegistryBootstrap#registerProvider(MentionProvider)}
 * during mod bootstrap to participate in mention registration.
 */
public interface MentionProvider {
    /**
     * Register one or more mention groups with the {@link MentionRegistry}.
     *
     * <p>Providers are executed on the main thread during mod initialization, so implementations should avoid
     * heavy work. Providers are expected to call {@link MentionRegistry#register(MentionGroup)} for each group
     * they contribute.</p>
     */
    default void registerMentionGroups() {
    }

    /**
     * Register one or more mention functions with the {@link MentionRegistry}.
     *
     * <p>Providers are executed on the main thread during mod initialization, so implementations should avoid
     * heavy work. Providers are expected to call {@link MentionRegistry#register(MentionFunction)} for each
     * function they contribute.</p>
     */
    default void registerMentionFunctions() {
    }
}
