package moe.caa.multilogin.core.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * 值操作工具类
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ValueUtil {

    /**
     * 判断字符串是否为空
     * @param str 需要判断的字符串
     * @return 字符串是否为空
     */
    public static boolean isEmpty(String str){
        return str == null || str.length() == 0;
    }

    /**
     * 字符数组变字符串
     * @param chars 字符数组
     * @return 字符串
     */
    public static String charArrayToString(char[] chars){
        StringBuilder sb = new StringBuilder();
        for (char c : chars) {
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * 前者为空返回前者，否则返回后者
     * @param val 前者
     * @param def 后者
     * @return 前者为空返回前者，否则返回后者
     */
    public static <R> R getOrDef(R val, R def) {
        return val == null ? def : val;
    }
}
