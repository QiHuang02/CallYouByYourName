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
                PlayAtSoundPacket.class,
                PlayAtSoundPacket::encode,
                PlayAtSoundPacket::decode,
                PlayAtSoundPacket::handle
        );

        net.registerMessage(
                id(),
                SyncMentionGroupsPacket.class,
                SyncMentionGroupsPacket::encode,
                SyncMentionGroupsPacket::decode,
                SyncMentionGroupsPacket::handle
        );
    }

    public static SimpleChannel getChannel() {
        return INSTANCE;
    }
}
