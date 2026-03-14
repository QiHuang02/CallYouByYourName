package cn.qihuang02.callyou.core.attachment;

import cn.qihuang02.callyou.api.MentionType;
import cn.qihuang02.callyou.api.components.MentionRules;
import cn.qihuang02.callyou.api.components.Notifier;
import cn.qihuang02.callyou.registry.CallYouMentionRegistries;
import cn.qihuang02.callyou.registry.CallYouRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;

public final class MentionPreferences {
    public static final Codec<MentionPreferences> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.listOf().optionalFieldOf("blocked_senders", Collections.emptyList())
                    .forGetter(preferences -> new ArrayList<>(preferences.blockedSenders)),
            ResourceLocation.CODEC.listOf().optionalFieldOf("blocked_types", Collections.emptyList())
                    .forGetter(preferences -> new ArrayList<>(preferences.blockedTypes)),
            Codec.unboundedMap(ResourceLocation.CODEC, TypePreference.CODEC)
                    .optionalFieldOf("type_preferences", Collections.emptyMap())
                    .forGetter(preferences -> new HashMap<>(preferences.typePreferences)),
            Codec.BOOL.optionalFieldOf("allow_mentions", true).forGetter(preferences -> preferences.allowMentions),
            Codec.BOOL.optionalFieldOf("allow_mass_mentions", true).forGetter(preferences -> preferences.allowMassMentions)
    ).apply(instance, MentionPreferences::fromCodec));

    final Set<UUID> blockedSenders = new HashSet<>();
    private final Set<ResourceLocation> blockedTypes = new HashSet<>();
    private final Map<ResourceLocation, TypePreference> typePreferences = new HashMap<>();
    private boolean allowMentions = true;
    private boolean allowMassMentions = true;

    public MentionPreferences() {
    }

    private static @NotNull MentionPreferences fromCodec(
            @NotNull List<UUID> blockedSenders,
            @NotNull List<ResourceLocation> blockedTypes,
            @NotNull Map<ResourceLocation, TypePreference> typePreferences,
            boolean allowMentions,
            boolean allowMassMentions
    ) {
        MentionPreferences preferences = new MentionPreferences();
        preferences.allowMentions = allowMentions;
        preferences.allowMassMentions = allowMassMentions;
        for (UUID sender : blockedSenders) {
            if (sender != null) {
                preferences.blockedSenders.add(sender);
            }
        }
        for (ResourceLocation type : blockedTypes) {
            if (type != null) {
                preferences.blockedTypes.add(type);
            }
        }
        for (Map.Entry<ResourceLocation, TypePreference> entry : typePreferences.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                preferences.typePreferences.put(entry.getKey(), entry.getValue());
            }
        }
        return preferences;
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
    public @NotNull @UnmodifiableView Set<ResourceLocation> getBlockedTypes() {
        return Collections.unmodifiableSet(blockedTypes);
    }

    @Contract(pure = true)
    public @NotNull @UnmodifiableView Set<UUID> getBlockedSenders() {
        return Collections.unmodifiableSet(blockedSenders);
    }

    public void blockMentionType(ResourceLocation typeId) {
        if (typeId != null) {
            blockedTypes.add(typeId);
        }
    }

    public void unblockMentionType(ResourceLocation typeId) {
        if (typeId != null) {
            blockedTypes.remove(typeId);
        }
    }

    public void setNotifierEnabled(ResourceLocation mentionTypeId, ResourceLocation notifierTypeId, boolean enabled) {
        typePreferences.put(mentionTypeId, getPreference(mentionTypeId).withNotifier(notifierTypeId, enabled));
    }

    public boolean isNotifierEnabled(ResourceLocation mentionTypeId, ResourceLocation notifierTypeId) {
        return getPreference(mentionTypeId).isNotifierEnabled(notifierTypeId);
    }

    /**
     * Checks if the notifier is enabled for the given mention type, resolving the Notifier ID from the registry.
     */
    public boolean isNotifierEnabled(ResourceLocation mentionTypeId) {
        MentionType mentionType = CallYouMentionRegistries.get(mentionTypeId);
        if (mentionType == null) return true;

        Notifier notifier = mentionType.notifier();
        Notifier.NotifierType notifierType = notifier.type();

        ResourceLocation notifierId = null;
        for (Map.Entry<ResourceLocation, Notifier.NotifierType> entry : CallYouRegistries.notifierTypes().entrySet()) {
            if (entry.getValue().equals(notifierType)) {
                notifierId = entry.getKey();
                break;
            }
        }
        if (notifierId == null) return true;

        return isNotifierEnabled(mentionTypeId, notifierId);
    }

    private TypePreference getPreference(ResourceLocation typeId) {
        return typePreferences.getOrDefault(typeId, TypePreference.DEFAULT);
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
        this.blockedTypes.clear();
        this.blockedSenders.clear();
        this.typePreferences.clear();
    }

    public void copyFrom(@NotNull MentionPreferences other) {
        this.allowMentions = other.allowMentions;
        this.allowMassMentions = other.allowMassMentions;

        this.blockedTypes.clear();
        for (ResourceLocation id : other.blockedTypes) {
            if (id != null) {
                this.blockedTypes.add(id);
            }
        }

        this.blockedSenders.clear();
        for (UUID uuid : other.blockedSenders) {
            if (uuid != null) {
                this.blockedSenders.add(uuid);
            }
        }

        this.typePreferences.clear();
        this.typePreferences.putAll(other.typePreferences);
    }

    public boolean isMentionAllowed(
            @NotNull MentionType type,
            @Nullable ResourceLocation typeId,
            @NotNull UUID senderId
    ) {
        if (!allowMentions) {
            return false;
        }

        if (blockedSenders.contains(senderId)) {
            return false;
        }

        if (typeId != null && blockedTypes.contains(typeId)) {
            return false;
        }

        MentionRules rules = type.rules();
        return allowMassMentions || rules == null || !rules.isMass();
    }

    public static final class TypePreference {
        public static final TypePreference DEFAULT = new TypePreference(Collections.emptyMap());
        public static final Codec<TypePreference> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.unboundedMap(ResourceLocation.CODEC, Codec.BOOL)
                        .optionalFieldOf("notifiers", Collections.emptyMap())
                        .forGetter(preference -> preference.enabledNotifiers)
        ).apply(instance, TypePreference::new));

        private Map<ResourceLocation, Boolean> enabledNotifiers = new HashMap<>();

        public TypePreference() {
            this(Collections.emptyMap());
        }

        public TypePreference(Map<ResourceLocation, Boolean> enabledNotifiers) {
            this.enabledNotifiers = Collections.unmodifiableMap(new HashMap<>(enabledNotifiers));
        }

        @Contract("_, _ -> new")
        public @NotNull TypePreference withNotifier(ResourceLocation notifierId, boolean enabled) {
            Map<ResourceLocation, Boolean> newMap = new HashMap<>(enabledNotifiers);
            newMap.put(notifierId, enabled);
            return new TypePreference(Collections.unmodifiableMap(newMap));
        }

        public boolean isNotifierEnabled(ResourceLocation notifierId) {
            return enabledNotifiers.getOrDefault(notifierId, true);
        }
    }
}
