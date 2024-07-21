package moe.caa.multilogin.bungee.injector.redirect.chat;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.md_5.bungee.connection.CancelSendSignal;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.ProtocolConstants;

import java.time.Instant;
import java.util.UUID;

@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerSessionPacketBlocker extends DefinedPacket {
    private UUID sessionId;
    private Instant expires;
    private byte[] publicKey;
    private byte[] signature;

    @Override
    public void handle(AbstractPacketHandler handler) {
        throw CancelSendSignal.INSTANCE;
    }

    @Override
    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion) {
        sessionId = readUUID(buf);
        expires = Instant.ofEpochMilli(buf.readLong());
        publicKey = readArray(buf);
        signature = readArray(buf, 4096);
    }

    @Override
    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion) {
        writeUUID(sessionId, buf);
        buf.writeLong(expires.getEpochSecond());
        writeArray(publicKey, buf);
        writeArray(signature, buf);
    }
}
