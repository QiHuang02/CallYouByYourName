package cn.qihuang02.cyyn.network;

import cn.qihuang02.cyyn.CallYouByYourName;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class CYYNMessages {
    private static final String PROTOCOL_VERSION = "1.2";
    private static SimpleChannel INSTANCE;
    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }

    public static void register() {
        SimpleChannel net = NetworkRegistry.newSimpleChannel(
                CallYouByYourName.getRl("message"),
                () -> PROTOCOL_VERSION,
                PROTOCOL_VERSION::equals,
                PROTOCOL_VERSION::equals
        );

        INSTANCE = net;

        net.registerMessage(
                id(),
                ClientboundPlayAtSoundPacket.class,
                ClientboundPlayAtSoundPacket::encode,
                ClientboundPlayAtSoundPacket::decode,
                ClientboundPlayAtSoundPacket::handle
        );

        net.registerMessage(
                id(),
                ClientboundSyncMentionGroupsPacket.class,
                ClientboundSyncMentionGroupsPacket::encode,
                ClientboundSyncMentionGroupsPacket::decode,
                ClientboundSyncMentionGroupsPacket::handle
        );
    }

    public static SimpleChannel getChannel() {
        return INSTANCE;
    }
}
