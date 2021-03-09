package moe.caa.multilogin.core.util;

import moe.caa.multilogin.core.MultiCore;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class I18n {
    private static ResourceBundle rb;

    public static void initService() {
        rb = ResourceBundle.getBundle("lang/multilogin");
        MultiCore.info(getTransString("load_language", Locale.getDefault()));
    }

    public static String getTransString(String key, Object... args) {
        return MessageFormat.format(rb.getString(key), args);
    }
}
