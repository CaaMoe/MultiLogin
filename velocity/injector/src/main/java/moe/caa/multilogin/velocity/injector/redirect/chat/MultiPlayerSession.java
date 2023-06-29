package moe.caa.multilogin.velocity.injector.redirect.chat;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;

import java.time.Instant;
import java.util.UUID;

public class MultiPlayerSession implements MinecraftPacket {

    public MultiPlayerSession(){

    }

    @Override
    public void decode(ByteBuf byteBuf, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        UUID uuid = new UUID(byteBuf.readLong(), byteBuf.readLong());
        Instant instant = Instant.ofEpochMilli(byteBuf.readLong());
        byte[] publicKey = readByteArray(byteBuf, 512);
        byte[] bs = readByteArray(byteBuf, 4096);
    }

    @Override
    public void encode(ByteBuf byteBuf, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {

    }

    public byte[] readByteArray(ByteBuf buf, int maxSize) {
        int i = readVarInt(buf);
        if (i > maxSize) {
            throw new DecoderException("ByteArray with size " + i + " is bigger than allowed " + maxSize);
        } else {
            byte[] bs = new byte[i];
            buf.readBytes(bs);
            return bs;
        }
    }

    public int readVarInt(ByteBuf buf) {
        int i = 0;
        int j = 0;

        byte b;
        do {
            b = buf.readByte();
            i |= (b & 127) << j++ * 7;
            if (j > 5) {
                throw new RuntimeException("VarInt too big");
            }
        } while((b & 128) == 128);

        return i;
    }


    @Override
    public boolean handle(MinecraftSessionHandler minecraftSessionHandler) {
        return true;
    }
}
