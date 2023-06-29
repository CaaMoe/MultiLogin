package moe.caa.multilogin.velocity.injector.redirect.chat;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import moe.caa.multilogin.api.logger.LoggerProvider;

import java.time.Instant;
import java.util.UUID;

public class MultiPlayerSession implements MinecraftPacket {
    private UUID sessionId;
    private Instant expires;
    private byte[] publicKey;
    private byte[] signature;

    public MultiPlayerSession(){

    }

    @Override
    public void decode(ByteBuf byteBuf, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        sessionId = ProtocolUtils.readUuid(byteBuf);
        expires = Instant.ofEpochMilli(byteBuf.readLong());
        publicKey = ProtocolUtils.readByteArray(byteBuf, 512);
        signature = ProtocolUtils.readByteArray(byteBuf, 4096);
    }

    @Override
    public void encode(ByteBuf byteBuf, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        ProtocolUtils.writeUuid(byteBuf, sessionId);
        byteBuf.writeLong(expires.getEpochSecond());
        ProtocolUtils.writeByteArray(byteBuf, publicKey);
        ProtocolUtils.writeByteArray(byteBuf, signature);
    }

    @Override
    public boolean handle(MinecraftSessionHandler minecraftSessionHandler) {
        LoggerProvider.getLogger().debug("Player session ignored: " + sessionId);
        return true;
    }
}
