package moe.caa.multilogin.core.logger;

import moe.caa.multilogin.core.language.LanguageKeys;
import moe.caa.multilogin.core.main.MultiCore;

import java.util.logging.Level;

public class MultiLogger {
    private static boolean debug = true;
    private static Log4JCore log4JCore;

    public static void init() {

        try {
            debug = MultiCore.config.get("debug", Boolean.class, true);
            Log4JCore l4c = new Log4JCore();
            l4c.init();
//            防止提前赋值造成不可用
            log4JCore = l4c;
        } catch (Exception e) {
            log(LoggerLevel.ERROR, e);
            log(LoggerLevel.ERROR, LanguageKeys.LOGGER_FILE_ERROR.getMessage());
            debug = true;
        }
        if (debug) log(LoggerLevel.INFO, LanguageKeys.DEBUG_ENABLE.getMessage());
    }


    /**
     * 记录Throwable
     *
     * @param level 日志等级
     * @param t     Throwable
     */
    public static void log(LoggerLevel level, Throwable t) {
        log(level, "Throwable", t);
    }

    public static void log(LoggerLevel level, String message) {
        log(level, message, null);
    }

    public static void log(LoggerLevel level, String message, Throwable throwable) {
        logDefault(level, message, throwable);
        if (log4JCore != null)
            log4JCore.log(level, message, throwable);
    }

    private static void logDefault(LoggerLevel level, String message, Throwable throwable) {
        switch (level) {
            case INFO:
                MultiCore.plugin.getLogger().log(Level.INFO, message, throwable);
                break;
            case ERROR:
                MultiCore.plugin.getLogger().log(Level.SEVERE, message, throwable);
                break;
            case WARN:
                MultiCore.plugin.getLogger().log(Level.WARNING, message, throwable);
                break;
            case DEBUG:
                if (debug)
                    MultiCore.plugin.getLogger().log(Level.INFO, "[DEBUG] " + message, throwable);
                break;
        }
    }

}
