package moe.caa.multilogin.bungee.injector.redirect.chat;

import io.netty.buffer.ByteBuf;
import moe.caa.multilogin.api.logger.LoggerProvider;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.ProtocolConstants;

import java.time.Instant;
import java.util.UUID;

public class MultiPlayerSession extends DefinedPacket {
    private UUID sessionId;
    private Instant expires;
    private byte[] publicKey;
    private byte[] signature;

    @Override
    public void handle(AbstractPacketHandler handler) {
        LoggerProvider.getLogger().debug("Player session ignored: " + sessionId);
    }

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion) {
        sessionId = readUUID(buf);
        expires = Instant.ofEpochMilli(buf.readLong());
        publicKey =readArray(buf, 512);
        signature = readArray(buf, 4096);
    }

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion) {
        writeUUID(sessionId, buf);
        buf.writeLong(expires.getEpochSecond());
        writeArray(publicKey, buf);
        writeArray(signature, buf);
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public String toString() {
        return null;
    }
}
