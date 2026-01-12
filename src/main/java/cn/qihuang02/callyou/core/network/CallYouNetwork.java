package cn.qihuang02.callyou.core.network;

import cn.qihuang02.callyou.CallYouByYourName;
import cn.qihuang02.callyou.core.network.payload.c2s.MentionLogActionPayload;
import cn.qihuang02.callyou.core.network.payload.c2s.MentionLogRequestPayload;
import cn.qihuang02.callyou.core.network.payload.c2s.MentionPrefsRequestPayload;
import cn.qihuang02.callyou.core.network.payload.c2s.MentionPrefsUpdatePayload;
import cn.qihuang02.callyou.core.network.payload.s2c.MentionLogResponsePayload;
import cn.qihuang02.callyou.core.network.payload.s2c.MentionPrefsSyncPayload;
import cn.qihuang02.callyou.core.network.payload.s2c.MentionToastPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(modid = CallYouByYourName.MODID)
public final class CallYouNetwork {
    private static final String PROTOCOL_VERSION = "1";

    @SubscribeEvent
    public static void registerPayloadHandlers(final @NotNull RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(CallYouByYourName.MODID).versioned(PROTOCOL_VERSION);
        registrar.playToClient(MentionPrefsSyncPayload.TYPE, MentionPrefsSyncPayload.STREAM_CODEC, NetworkHandler::handleSync);
        registrar.playToServer(MentionPrefsUpdatePayload.TYPE, MentionPrefsUpdatePayload.STREAM_CODEC, NetworkHandler::handleUpdate);
        registrar.playToServer(MentionPrefsRequestPayload.TYPE, MentionPrefsRequestPayload.STREAM_CODEC, NetworkHandler::handleRequest);
        registrar.playToClient(MentionToastPayload.TYPE, MentionToastPayload.STREAM_CODEC, NetworkHandler::handleToast);
        registrar.playToServer(MentionLogRequestPayload.TYPE, MentionLogRequestPayload.STREAM_CODEC, NetworkHandler::handleLogRequest);
        registrar.playToClient(MentionLogResponsePayload.TYPE, MentionLogResponsePayload.STREAM_CODEC, NetworkHandler::handleLogResponse);
        registrar.playToServer(MentionLogActionPayload.TYPE, MentionLogActionPayload.STREAM_CODEC, NetworkHandler::handleLogAction);
    }
}
