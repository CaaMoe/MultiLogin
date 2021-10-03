package moe.caa.multilogin.core.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.logger.MultiLogger;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.UUID;

/**
 * 值操作工具类
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
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
        } catch (Exception e) {
            MultiLogger.getLogger().log(LoggerLevel.DEBUG, String.format("Invalid UUID string: %s", uuid), e);
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

    /**
     * 字符数组变字符串
     *
     * @param chars 字符数组
     * @return 字符串
     */
    public static String charArrayToString(char[] chars) {
        StringBuilder sb = new StringBuilder();
        for (char c : chars) {
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * 前者为空返回前者，否则返回后者
     *
     * @param val 前者
     * @param def 后者
     * @return 前者为空返回前者，否则返回后者
     */
    public static <R> R getOrDef(R val, R def) {
        return val == null ? def : val;
    }

    /**
     * 占位填充数据
     *
     * @param source  源字符串
     * @param content 填充内容
     * @return 完善后的字符串
     */
    public static String format(String source, FormatContent content) {
        List<FormatContent.FormatEntry> entries = content.getFormatEntries();
        for (int i = 0; i < entries.size(); i++) {
            source = source.replace("{" + i + "}", entries.get(i).getContent().toString());
            source = source.replace("{" + entries.get(i).getName() + "}", entries.get(i).getContent().toString());
        }
        return source;
    }

    /**
     * 判断一个字符串是不是一个布尔值
     *
     * @param value 字符串
     * @return 是不是一个布尔值
     */
    public static boolean isBoolValue(String value) {
        return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false");
    }
}
