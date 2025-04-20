package moe.caa.multilogin.velocity.injector.redirect.chat;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.crypto.IdentifiedKey;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import io.netty.buffer.ByteBuf;
import moe.caa.multilogin.api.internal.logger.LoggerProvider;

import java.util.UUID;

public class PlayerSessionPacketBlocker implements MinecraftPacket {
    private UUID sessionId;
    private IdentifiedKey identifiedKey;
    private boolean hasKey = true;

    public PlayerSessionPacketBlocker(){

    }

    @Override
    public void decode(ByteBuf byteBuf, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        byteBuf.markReaderIndex();
        try {
            sessionId = ProtocolUtils.readUuid(byteBuf);
            identifiedKey = ProtocolUtils.readPlayerKey(protocolVersion, byteBuf);
        } catch (Throwable t) {
            byteBuf.resetReaderIndex();
            LoggerProvider.getLogger().debug("Failed to decode player session packet.", t);
            hasKey = false;
        }
    }

    @Override
    public void encode(ByteBuf byteBuf, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        //不发送ChatSession
        if (hasKey) {
            ProtocolUtils.writeUuid(byteBuf, sessionId);
            ProtocolUtils.writePlayerKey(byteBuf, identifiedKey);
        }
    }

    @Override
    public boolean handle(MinecraftSessionHandler minecraftSessionHandler) {
        return true;
    }
}
