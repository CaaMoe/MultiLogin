package moe.caa.multilogin.velocity.injector.redirect.chat;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.crypto.IdentifiedKey;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import io.netty.buffer.ByteBuf;

import java.util.UUID;

public class PlayerSessionPacketBlocker implements MinecraftPacket {
    private UUID sessionId;
    private IdentifiedKey identifiedKey;
    private boolean hasKey = false;

    public PlayerSessionPacketBlocker(){

    }

    @Override
    public void decode(ByteBuf byteBuf, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        try {
            sessionId = ProtocolUtils.readUuid(byteBuf);
            identifiedKey = ProtocolUtils.readPlayerKey(protocolVersion, byteBuf);
        } catch (Exception ignore) {
            hasKey = false;
        }
    }

    @Override
    public void encode(ByteBuf byteBuf, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        try {
            //不发送ChatSession
            if (hasKey) {
                ProtocolUtils.writeUuid(byteBuf, sessionId);
                ProtocolUtils.writePlayerKey(byteBuf, identifiedKey);
            }
        } catch (Exception ignore) {
            //idk why does it throw the `java.lang.IllegalArgumentException`
            //Caused by: java.lang.IllegalArgumentException: Unknown node type 3
            //        at com.velocitypowered.proxy.protocol.packet.AvailableCommandsPacket.deserializeNode(AvailableCommandsPacket.java:219)
            //        at com.velocitypowered.proxy.protocol.packet.AvailableCommandsPacket.decode(AvailableCommandsPacket.java:88)
            //        at com.velocitypowered.proxy.protocol.netty.MinecraftDecoder.tryDecode(MinecraftDecoder.java:83)
        }
    }

    @Override
    public boolean handle(MinecraftSessionHandler minecraftSessionHandler) {
        return true;
    }
}
