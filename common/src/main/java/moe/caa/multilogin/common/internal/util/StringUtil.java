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

    public static boolean isReasonablePlayerName(String name) {
        if (name == null || name.isEmpty() || name.length() > 16) return false;
        for (char c : name.toCharArray()) {
            if (!(Character.isLetterOrDigit(c) || c == '_')) {
                return false;
            }
        }
        return true;
    }
}
