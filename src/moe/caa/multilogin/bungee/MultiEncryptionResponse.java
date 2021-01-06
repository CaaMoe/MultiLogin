package moe.caa.multilogin.bungee;

import io.netty.buffer.ByteBuf;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.ProtocolConstants;
import net.md_5.bungee.protocol.packet.EncryptionResponse;

import java.util.Arrays;

public class MultiEncryptionResponse extends EncryptionResponse {
    private byte[] sharedSecret;
    private byte[] verifyToken;

    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion) {
        this.sharedSecret = readArray(buf, 128);
        this.verifyToken = readArray(buf, 128);
    }

    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion) {
        writeArray(this.sharedSecret, buf);
        writeArray(this.verifyToken, buf);
    }

    public void handle(AbstractPacketHandler handler) throws Exception {
        System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        handler.handle(this);
    }

    public byte[] getSharedSecret() {
        return this.sharedSecret;
    }

    public byte[] getVerifyToken() {
        return this.verifyToken;
    }

    public void setSharedSecret(byte[] sharedSecret) {
        this.sharedSecret = sharedSecret;
    }

    public void setVerifyToken(byte[] verifyToken) {
        this.verifyToken = verifyToken;
    }

    public String toString() {
        return "EncryptionResponse(sharedSecret=" + Arrays.toString(this.getSharedSecret()) + ", verifyToken=" + Arrays.toString(this.getVerifyToken()) + ")";
    }

    public MultiEncryptionResponse() {
    }

    public MultiEncryptionResponse(byte[] sharedSecret, byte[] verifyToken) {
        this.sharedSecret = sharedSecret;
        this.verifyToken = verifyToken;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof EncryptionResponse)) {
            return false;
        } else {
            MultiEncryptionResponse other = (MultiEncryptionResponse)o;
            if (!other.canEqual(this)) {
                return false;
            } else if (!Arrays.equals(this.getSharedSecret(), other.getSharedSecret())) {
                return false;
            } else {
                return Arrays.equals(this.getVerifyToken(), other.getVerifyToken());
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof MultiEncryptionResponse;
    }

    public int hashCode() {
        int result = 1;
        result = result * 59 + Arrays.hashCode(this.getSharedSecret());
        result = result * 59 + Arrays.hashCode(this.getVerifyToken());
        return result;
    }
}
