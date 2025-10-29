package cn.qihuang02.cyyn.server.mention;

import cn.qihuang02.cyyn.CallYouByYourName;
import cn.qihuang02.cyyn.api.mention.MentionProvider;
import cn.qihuang02.cyyn.api.mention.MentionRegistry;
import cn.qihuang02.cyyn.server.mention.function.ItemMentionFunction;
import cn.qihuang02.cyyn.server.mention.function.SpotMentionFunction;
import cn.qihuang02.cyyn.server.mention.group.HereMentionGroup;
import cn.qihuang02.cyyn.server.mention.group.NearMentionGroup;
import org.slf4j.Logger;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Central bootstrap responsible for registering built-in mention groups and functions and invoking
 * {@link MentionProvider} implementations registered via {@link #registerProvider(MentionProvider)}.
 */
public final class MentionRegistryBootstrap {
    private static final AtomicBoolean BOOTSTRAPPED = new AtomicBoolean();
    private static final Logger LOGGER = CallYouByYourName.LOGGER;
    private static final Queue<MentionProvider> PROVIDERS = new ConcurrentLinkedQueue<>();

    private MentionRegistryBootstrap() {
    }

    public static void bootstrap() {
        if (!BOOTSTRAPPED.compareAndSet(false, true)) {
            return;
        }

        MentionRegistry.register(new HereMentionGroup());
        MentionRegistry.register(new NearMentionGroup());
        MentionRegistry.register(new ItemMentionFunction());
        MentionRegistry.register(new SpotMentionFunction());

        MentionProvider provider;
        while ((provider = PROVIDERS.poll()) != null) {
            invokeProvider(provider);
        }
    }

    public static void registerProvider(MentionProvider provider) {
        Objects.requireNonNull(provider, "provider");

        if (BOOTSTRAPPED.get()) {
            invokeProvider(provider);
            return;
        }

        PROVIDERS.add(provider);
    }

    private static void invokeProvider(MentionProvider provider) {
        try {
            provider.registerMentionGroups();
        } catch (RuntimeException ex) {
            LOGGER.error("Failed to register mention groups via provider {}", provider.getClass().getName(), ex);
        }

        try {
            provider.registerMentionFunctions();
        } catch (RuntimeException ex) {
            LOGGER.error("Failed to register mention functions via provider {}", provider.getClass().getName(), ex);
        }
    }
}
