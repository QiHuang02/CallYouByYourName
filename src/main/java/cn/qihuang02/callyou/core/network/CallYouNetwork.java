package cn.qihuang02.callyou.core.network;

import cn.qihuang02.callyou.api.components.Notifier;
import cn.qihuang02.callyou.api.components.TargetProvider;
import cn.qihuang02.callyou.api.components.TextFormatter;
import cn.qihuang02.callyou.core.attachment.MentionPreferences;
import cn.qihuang02.callyou.core.saveddata.MentionRecord;
import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacketDistributor;
import com.lowdragmc.lowdraglib2.syncdata.AccessorRegistries;
import com.lowdragmc.lowdraglib2.syncdata.accessor.direct.CustomDirectAccessor;
import net.minecraft.network.codec.ByteBufCodecs;

public final class CallYouNetwork {
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
        AccessorRegistries.registerAccessor(
                CustomDirectAccessor.builder(TargetProvider.class)
                        .codec(TargetProvider.CODEC)
                        .streamCodec(ByteBufCodecs.fromCodecWithRegistries(TargetProvider.CODEC))
                        .build()
        );
        AccessorRegistries.registerAccessor(
                CustomDirectAccessor.builder(TextFormatter.class)
                        .codec(TextFormatter.CODEC)
                        .streamCodec(ByteBufCodecs.fromCodecWithRegistries(TextFormatter.CODEC))
                        .build()
        );
        AccessorRegistries.registerAccessor(
                CustomDirectAccessor.builder(Notifier.class)
                        .codec(Notifier.CODEC)
                        .streamCodec(ByteBufCodecs.fromCodecWithRegistries(Notifier.CODEC))
                        .build()
        );
    }

    public enum MentionLogAction {
        MARK_ALL_READ,
        DELETE_SINGLE
    }
}
