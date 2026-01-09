package cn.qihuang02.callyou.network;

import cn.qihuang02.callyou.core.attachment.MentionPreferences;
import cn.qihuang02.callyou.core.storage.MentionRecord;
import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacketDistributor;
import com.lowdragmc.lowdraglib2.syncdata.AccessorRegistries;
import com.lowdragmc.lowdraglib2.syncdata.accessor.direct.CustomDirectAccessor;
import net.minecraft.network.codec.ByteBufCodecs;

public final class CallYouNetwork {
    public enum MentionLogAction {
        MARK_ALL_READ,
        DELETE_SINGLE
    }

    private static boolean initialized;

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        registerAccessors();
        RPCPacketDistributor.init();
    }

    private static void registerAccessors() {
        AccessorRegistries.registerAccessor(
                CustomDirectAccessor.builder(MentionPreferences.class)
                        .codec(MentionPreferences.CODEC)
                        .streamCodec(ByteBufCodecs.fromCodec(MentionPreferences.CODEC))
                        .build()
        );
        AccessorRegistries.registerAccessor(
                CustomDirectAccessor.builder(MentionRecord.class)
                        .codec(MentionRecord.CODEC)
                        .streamCodec(ByteBufCodecs.fromCodecWithRegistries(MentionRecord.CODEC))
                        .build()
        );
    }
}
