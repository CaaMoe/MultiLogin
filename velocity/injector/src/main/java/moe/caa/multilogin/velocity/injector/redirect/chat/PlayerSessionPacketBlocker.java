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

    public PlayerSessionPacketBlocker(){

    }

    @Override
    public void decode(ByteBuf byteBuf, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        sessionId = ProtocolUtils.readUuid(byteBuf);
        identifiedKey = ProtocolUtils.readPlayerKey(protocolVersion, byteBuf);
    }

    @Override
    public void encode(ByteBuf byteBuf, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        ProtocolUtils.writeUuid(byteBuf, sessionId);
        ProtocolUtils.writePlayerKey(byteBuf, identifiedKey);
    }

    @Override
    public boolean handle(MinecraftSessionHandler minecraftSessionHandler) {
        return true;
    }
}
