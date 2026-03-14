package cn.qihuang02.callyou.registry;

import cn.qihuang02.callyou.api.MentionType;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class CallYouMentionRegistries {
    private static final Map<ResourceLocation, MentionType> MENTION_TYPES = new ConcurrentHashMap<>();

    private CallYouMentionRegistries() {}

    public static void register(ResourceLocation id, MentionType type) {
        if (MENTION_TYPES.putIfAbsent(id, type) != null) {
            throw new IllegalStateException("Duplicate MentionType registration: " + id);
        }
    }

    public static MentionType get(ResourceLocation id) {
        return MENTION_TYPES.get(id);
    }

    public static Map<ResourceLocation, MentionType> mentionTypes() {
        return Collections.unmodifiableMap(MENTION_TYPES);
    }

    public static Map<ResourceLocation, MentionType> mentionTypesMutable() {
        return MENTION_TYPES;
    }

    public static void clear() {
        MENTION_TYPES.clear();
    }
}
