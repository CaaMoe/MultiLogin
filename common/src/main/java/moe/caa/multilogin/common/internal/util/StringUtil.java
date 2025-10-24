package moe.caa.multilogin.common.internal.util;

public class StringUtil {
    public static String kebabCaseToUnderscoreUpperCase(String str) {
        return str.replace("-", "_").toUpperCase();
    }

    public static String underscoreUpperCaseToKebabCase(String str) {
        return str.replace("_", "-").toLowerCase();
    }

    public static boolean isNullOrEmpty(String input) {
        if (input == null) return true;
        return input.isEmpty();
    }
}
