package cn.qihuang02.cyyn.mention;

import cn.qihuang02.cyyn.CallYouByYourName;
import cn.qihuang02.cyyn.mention.group.HereMentionGroup;
import cn.qihuang02.cyyn.mention.group.NearMentionGroup;
import org.slf4j.Logger;

import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Central bootstrap responsible for registering built-in mention groups and invoking {@link MentionGroupProvider}
 * implementations discovered via {@link ServiceLoader}.
 */
public final class MentionGroupBootstrap {
    private static final AtomicBoolean BOOTSTRAPPED = new AtomicBoolean();
    private static final Logger LOGGER = CallYouByYourName.LOGGER;

    private MentionGroupBootstrap() {
    }

    /**
     * Initializes the mention group registry with built-in groups and third-party providers. Safe to call multiple
     * times; subsequent invocations are ignored.
     */
    public static void bootstrap() {
        if (!BOOTSTRAPPED.compareAndSet(false, true)) {
            return;
        }

        MentionGroupRegistry.register(new HereMentionGroup());
        MentionGroupRegistry.register(new NearMentionGroup());

        ServiceLoader<MentionGroupProvider> loader = ServiceLoader.load(MentionGroupProvider.class);
        for (MentionGroupProvider provider : loader) {
            try {
                provider.registerMentionGroups();
            } catch (ServiceConfigurationError | RuntimeException ex) {
                LOGGER.error("Failed to execute MentionGroupProvider: {}", provider.getClass().getName(), ex);
            }
        }
    }
}
