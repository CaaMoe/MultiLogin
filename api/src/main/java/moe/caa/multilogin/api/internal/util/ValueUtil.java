package moe.caa.multilogin.api.internal.util;

import org.jetbrains.annotations.ApiStatus;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;
import java.util.UUID;

/**
 * 值操作工具
 */
@ApiStatus.Internal
public class ValueUtil {
    /**
     * UUID 转 bytes
     *
     * @param uuid 需要转换的 uuid
     * @return 转换后的 bytes
     */
    public static byte[] uuidToBytes(UUID uuid) {
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
    public static UUID bytesToUuid(byte[] bytes) {
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
     * 通过字符串生成 UUID
     *
     * @param uuid 字符串
     * @return 匹配的 uuid， 否则为空
     */
    public static UUID getUuidOrNull(String uuid) {
        UUID ret = null;
        try {
            ret = UUID.fromString(uuid.replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
        } catch (Exception ignored) {
        }
        return ret;
    }

    /**
     * 判断字符串是否为空
     *
     * @param str 需要判断的字符串
     * @return 字符串是否为空
     */
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static String transPapi(String s, Pair<?, ?>... pairs) {
        for (int i = 0; i < pairs.length; i++) {
            s = s.replace("{" + pairs[i].getValue1() + "}", pairs[i].getValue2() + "");
            s = s.replace("{" + i + "}", pairs[i].getValue2() + "");
        }
        return s;
    }

    /**
     * 替换变量
     */
    public static String transPapi(String s, List<Pair<?, ?>> pairs) {
        for (int i = 0; i < pairs.size(); i++) {
            s = s.replace("{" + pairs.get(i).getValue1() + "}", pairs.get(i).getValue2().toString());
            s = s.replace("{" + i + "}", pairs.get(i).getValue2().toString());
        }
        return s;
    }

    /**
     * 字符串加入
     */
    public static String join(CharSequence delimiter, CharSequence lastDelimiter, Object... elements) {
        if (elements.length == 0) return "";
        if (elements.length == 1) return elements[0].toString();
        StringJoiner joiner = new StringJoiner(delimiter);
        for (int i = 0; i < elements.length - 1; i++) {
            joiner.add(elements[i].toString());
        }
        return joiner.toString() + lastDelimiter + elements[elements.length - 1];
    }

    public static String join(CharSequence delimiter, CharSequence lastDelimiter, Collection<? extends Object> elements) {
        return join(delimiter, lastDelimiter, elements.toArray(new Object[0]));
    }

    /**
     * 返回字符串 sha256
     */
    public static byte[] sha256(String str) throws NoSuchAlgorithmException {
        return MessageDigest.getInstance("SHA-256").digest(str.getBytes(StandardCharsets.UTF_8));
    }

    public static UUID xuidToUUID(String xuid) {
        return new UUID(0, Long.parseLong(xuid));
    }

    public static String generateLinkCode(){
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            builder.append((int)(10 * Math.random()));
        }
        return builder.toString();
    }
}
