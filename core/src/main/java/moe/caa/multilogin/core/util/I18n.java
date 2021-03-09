package moe.caa.multilogin.core.util;

import moe.caa.multilogin.core.MultiCore;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class I18n {
    private static ResourceBundle rb;

    public static void initService() {
        try {
            rb = ResourceBundle.getBundle("lang/multilogin");
            MultiCore.info(getTransString("load_language", Locale.getDefault()));
        } catch (Exception e) {
            rb = ResourceBundle.getBundle("lang/multilogin", Locale.CHINA);
            MultiCore.info(getTransString("load_default_language", Locale.getDefault()));
        }
    }

    public static String getTransString(String key, Object... args) {
        return MessageFormat.format(rb.getString(key), args);
    }
}
