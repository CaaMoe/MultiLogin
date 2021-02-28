package moe.caa.multilogin.core.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

public class UUIDSerializer {
    public static byte[] uuidToByte(UUID uuid) {
        byte[] uuidBytes = new byte[16];
        ByteBuffer.wrap(uuidBytes)
                .order(ByteOrder.BIG_ENDIAN)
                .putLong(uuid.getMostSignificantBits())
                .putLong(uuid.getLeastSignificantBits());
        return uuidBytes;
    }

    public static UUID toUUID(byte[] bytes) {
        if (bytes.length != 16) {
            throw new IllegalArgumentException();
        }
        int i = 0;
        long msl = 0;
        for (; i < 8; i++) {
            msl = (msl << 8) | (bytes[i] & 0xFF);
        }
        long lsl = 0;
        for (; i < 16; i++) {
            lsl = (lsl << 8) | (bytes[i] & 0xFF);
        }
        return new UUID(msl, lsl);
    }
}
