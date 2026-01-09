package cn.qihuang02.callyou.network;

import cn.qihuang02.callyou.core.attachment.MentionPreferences;
import cn.qihuang02.callyou.core.storage.MentionRecord;
import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacketDistributor;
import com.lowdragmc.lowdraglib2.syncdata.AccessorRegistries;
import com.lowdragmc.lowdraglib2.syncdata.accessor.direct.CustomDirectAccessor;

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
                        .streamCodec(MentionPreferences.STREAM_CODEC)
                        .build()
        );
        AccessorRegistries.registerAccessor(
                CustomDirectAccessor.builder(MentionRecord.class)
                        .codec(MentionRecord.CODEC)
                        .streamCodec(MentionRecord.STREAM_CODEC)
                        .build()
        );
    }
}
