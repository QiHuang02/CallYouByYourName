package cn.qihuang02.callyou.core.attachment;

import cn.qihuang02.callyou.api.components.MentionRules;
import cn.qihuang02.callyou.api.MentionType;
import cn.qihuang02.callyou.api.components.Notifier;
import cn.qihuang02.callyou.registry.CallYouMentionRegistries;
import cn.qihuang02.callyou.registry.CallYouRegistries;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.syncdata.annotation.SkipPersistedValue;
import com.lowdragmc.lowdraglib2.utils.PersistedParser;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;

public final class MentionPreferences implements IPersistedSerializable {
    public static final class TypePreference implements IPersistedSerializable {
        public static final TypePreference DEFAULT = new TypePreference(Map.of());
        public static final Codec<TypePreference> CODEC = PersistedParser.createCodec(TypePreference::new);

        @Persisted(key = "notifiers")
        private Map<ResourceLocation, Boolean> enabledNotifiers = new HashMap<>();

        @SkipPersistedValue(field = "enabledNotifiers")
        private boolean skipEmptyNotifiers(Map<ResourceLocation, Boolean> notifiers) {
            return notifiers.isEmpty();
        }

        public TypePreference() {
            this(Map.of());
        }

        public TypePreference(Map<ResourceLocation, Boolean> enabledNotifiers) {
            this.enabledNotifiers = Map.copyOf(enabledNotifiers);
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

    public static final Codec<MentionPreferences> CODEC = PersistedParser.createCodec(MentionPreferences::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, MentionPreferences> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(CODEC);
    
    @Persisted(key = "blocked_senders")
    final Set<UUID> blockedSenders = new HashSet<>();
    @Persisted(key = "blocked_types")
    private final Set<ResourceLocation> blockedTypes = new HashSet<>();
    @Persisted(key = "type_preferences")
    private final Map<ResourceLocation, TypePreference> typePreferences = new HashMap<>();
    
    @Persisted(key = "allow_mentions")
    private boolean allowMentions = true;
    @Persisted(key = "allow_mass_mentions")
    private boolean allowMassMentions = true;

    @SkipPersistedValue(field = "blockedSenders")
    private boolean skipEmptySenders(Set<UUID> senders) {
        return senders.isEmpty();
    }

    @SkipPersistedValue(field = "blockedTypes")
    private boolean skipEmptyTypes(Set<ResourceLocation> types) {
        return types.isEmpty();
    }

    @SkipPersistedValue(field = "typePreferences")
    private boolean skipEmptyPreferences(Map<ResourceLocation, TypePreference> preferences) {
        return preferences.isEmpty();
    }

    @SkipPersistedValue(field = "allowMentions")
    private boolean skipDefaultAllowMentions(boolean value) {
        return value;
    }

    @SkipPersistedValue(field = "allowMassMentions")
    private boolean skipDefaultAllowMassMentions(boolean value) {
        return value;
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
