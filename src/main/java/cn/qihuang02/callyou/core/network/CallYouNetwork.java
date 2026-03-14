package cn.qihuang02.callyou.core.network;

import cn.qihuang02.callyou.core.network.packet.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class CallYouNetwork {
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("callyou", "main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );

    private static int id = 0;

    private CallYouNetwork() {}

    public static void register() {
        CHANNEL.registerMessage(id++, MentionPrefsSyncPacket.class,
                MentionPrefsSyncPacket::encode, MentionPrefsSyncPacket::decode, MentionPrefsSyncPacket::handle);
        CHANNEL.registerMessage(id++, MentionPrefsUpdatePacket.class,
                MentionPrefsUpdatePacket::encode, MentionPrefsUpdatePacket::decode, MentionPrefsUpdatePacket::handle);
        CHANNEL.registerMessage(id++, MentionPrefsRequestPacket.class,
                MentionPrefsRequestPacket::encode, MentionPrefsRequestPacket::decode, MentionPrefsRequestPacket::handle);
        CHANNEL.registerMessage(id++, MentionToastPacket.class,
                MentionToastPacket::encode, MentionToastPacket::decode, MentionToastPacket::handle);
        CHANNEL.registerMessage(id++, MentionLogRequestPacket.class,
                MentionLogRequestPacket::encode, MentionLogRequestPacket::decode, MentionLogRequestPacket::handle);
        CHANNEL.registerMessage(id++, MentionLogResponsePacket.class,
                MentionLogResponsePacket::encode, MentionLogResponsePacket::decode, MentionLogResponsePacket::handle);
        CHANNEL.registerMessage(id++, MentionLogActionPacket.class,
                MentionLogActionPacket::encode, MentionLogActionPacket::decode, MentionLogActionPacket::handle);
    }
}
