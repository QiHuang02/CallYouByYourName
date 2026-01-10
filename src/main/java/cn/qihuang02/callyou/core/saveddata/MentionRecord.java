package cn.qihuang02.callyou.core.saveddata;

import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.syncdata.annotation.SkipPersistedValue;
import com.lowdragmc.lowdraglib2.utils.PersistedParser;
import com.mojang.serialization.Codec;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class MentionRecord implements IPersistedSerializable {
    public static final Codec<MentionRecord> CODEC = PersistedParser.createCodec(MentionRecord::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, MentionRecord> STREAM_CODEC =
            ByteBufCodecs.fromCodecWithRegistries(CODEC);

    @Persisted(key = "sender_id")
    private UUID senderId = Util.NIL_UUID;

    @Persisted(key = "sender_name")
    private String senderName = "";

    @Persisted(key = "message")
    private Component message = Component.empty();

    @Persisted(key = "timestamp")
    private long timestamp;

    @Persisted(key = "read")
    private boolean read;

    @Persisted(key = "location")
    private GlobalPos location;

    @SkipPersistedValue(field = "location")
    private boolean skipMissingLocation(@Nullable GlobalPos pos) {
        return pos == null;
    }

    public MentionRecord() {
    }

    public MentionRecord(
            @NotNull UUID senderId,
            @NotNull String senderName,
            @NotNull Component message,
            long timestamp,
            boolean read,
            @Nullable GlobalPos location
    ) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.message = message;
        this.timestamp = timestamp;
        this.read = read;
        this.location = location;
    }

    public @NotNull UUID senderId() {
        return senderId;
    }

    public @NotNull String senderName() {
        return senderName;
    }

    public @NotNull Component message() {
        return message;
    }

    public long timestamp() {
        return timestamp;
    }

    public boolean read() {
        return read;
    }

    public @Nullable GlobalPos location() {
        return location;
    }

    public MentionRecord markRead() {
        if (this.read) {
            return this;
        }
        return new MentionRecord(senderId, senderName, message, timestamp, true, location);
    }
}
