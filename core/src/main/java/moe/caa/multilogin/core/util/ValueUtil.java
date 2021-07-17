package moe.caa.multilogin.core.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Base64;
import java.util.UUID;

/**
 * 对数据进行处理的工具类
 */
public class ValueUtil {
    public static final Base64.Decoder DECODER = Base64.getDecoder();

    /**
     * UUID 转 bytes
     *
     * @param uuid 需要转换的 uuid
     * @return 转换后的 bytes
     */
    public static byte[] uuidToByte(UUID uuid) {
        byte[] uuidBytes = new byte[16];
        ByteBuffer.wrap(uuidBytes).order(ByteOrder.BIG_ENDIAN).putLong(uuid.getMostSignificantBits()).putLong(uuid.getLeastSignificantBits());
        return uuidBytes;
    }

    /**
     * bytes 转 UUID
     *
     * @param bytes 需要转换的 bytes
     * @return 转换后的 UUID
     */
    public static UUID byteToUuid(byte[] bytes) {
        if (bytes.length != 16) return null;
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

    /**
     * 验证传入值 r 不为空，否则将引发 IllegalArgumentException 异常
     *
     * @param r   值
     * @param msg 自定义消息
     * @param <R> 你猜
     * @return 当前值
     */
    public static <R> R getOrThrow(R r, String msg) {
        if (r == null) {
            throw new IllegalArgumentException(msg);
        }
        return r;
    }

    public static <R> R getOrDef(R val, R def) {
        return val == null ? def : val;
    }

    public static boolean notIsEmpty(String str){
        if(str == null) return false;
        return str.length() != 0;
    }
}
