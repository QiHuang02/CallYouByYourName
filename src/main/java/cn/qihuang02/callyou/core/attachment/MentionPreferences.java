package cn.qihuang02.callyou.core.attachment;

import cn.qihuang02.callyou.api.MentionRules;
import cn.qihuang02.callyou.api.MentionType;
import cn.qihuang02.callyou.api.Notifier;
import cn.qihuang02.callyou.registry.CallYouMentionRegistries;
import cn.qihuang02.callyou.registry.CallYouRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;
import java.util.stream.Collectors;

public final class MentionPreferences {

    public record TypePreference(Map<ResourceLocation, Boolean> enabledNotifiers) {
        public static final TypePreference DEFAULT = new TypePreference(Map.of());
        public static final Codec<TypePreference> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.unboundedMap(ResourceLocation.CODEC, Codec.BOOL)
                                .optionalFieldOf("notifiers", Map.of())
                                .forGetter(TypePreference::enabledNotifiers)
                ).apply(instance, TypePreference::new)
        );

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

    public static final Codec<MentionPreferences> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.BOOL.optionalFieldOf("allow_mentions", true)
                            .forGetter(MentionPreferences::isAllowMentions),
                    Codec.BOOL.optionalFieldOf("allow_mass_mentions", true)
                            .forGetter(MentionPreferences::isAllowMassMentions),
                    ResourceLocation.CODEC.listOf().optionalFieldOf("blocked_types", List.of())
                            .forGetter(p -> new ArrayList<>(p.blockedTypes)),
                    Codec.STRING.listOf().optionalFieldOf("blocked_senders", List.of())
                            .forGetter(p -> p.blockedSenders.stream()
                                    .map(UUID::toString)
                                    .collect(Collectors.toList())),
                    Codec.unboundedMap(ResourceLocation.CODEC, TypePreference.CODEC)
                            .optionalFieldOf("type_preferences", Map.of())
                            .forGetter(p -> p.typePreferences)
            ).apply(instance, MentionPreferences::fromCodec)
    );
    
    final Set<UUID> blockedSenders = new HashSet<>();
    private final Set<ResourceLocation> blockedTypes = new HashSet<>();
    private final Map<ResourceLocation, TypePreference> typePreferences = new HashMap<>();
    
    private boolean allowMentions = true;
    private boolean allowMassMentions = true;

    private static @NotNull MentionPreferences fromCodec(
            boolean allowMentions,
            boolean allowMassMentions,
            @NotNull List<ResourceLocation> blockedTypes,
            List<String> blockedSenderStrings,
            Map<ResourceLocation, TypePreference> typePreferences
    ) {
        MentionPreferences prefs = new MentionPreferences();
        prefs.allowMentions = allowMentions;
        prefs.allowMassMentions = allowMassMentions;

        for (ResourceLocation id : blockedTypes) {
            prefs.blockMentionType(id);
        }
        for (String s : blockedSenderStrings) {
            try {
                prefs.blockedSenders.add(UUID.fromString(s));
            } catch (IllegalArgumentException ignored) {
            }
        }
        prefs.typePreferences.putAll(typePreferences);
        return prefs;
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
    public boolean isNotifierEnabled(ResourceLocation mentionTypeId, @Nullable RegistryAccess registryAccess) {
        if (registryAccess == null) return true;
        
        Registry<MentionType> mentionRegistry = registryAccess.registryOrThrow(CallYouMentionRegistries.MENTION_TYPE_REGISTRY_KEY);
        MentionType mentionType = mentionRegistry.get(mentionTypeId);
        if (mentionType == null) return true;
        
        Notifier notifier = mentionType.notifier();
        Notifier.NotifierType notifierType = notifier.type();
        
        ResourceLocation notifierId = CallYouRegistries.NOTIFICATION_RULE_TYPES.getKey(notifierType);
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
}