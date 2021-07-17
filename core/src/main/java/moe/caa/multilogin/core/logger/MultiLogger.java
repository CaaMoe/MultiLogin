package moe.caa.multilogin.core.logger;

import moe.caa.multilogin.core.language.LanguageKeys;
import moe.caa.multilogin.core.main.MultiCore;

import java.io.PrintWriter;
import java.io.StringWriter;

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

    public static void log(LoggerLevel level, String message) {
        logDefault(level, message);
        if (log4JCore != null)
            log4JCore.log(level, message, debug);
    }

    private static void logDefault(LoggerLevel level, String message) {
        switch (level) {
            case INFO:
                MultiCore.plugin.getLogger().info(message);
                break;
            case ERROR:
                MultiCore.plugin.getLogger().severe(message);
                break;
            case WARN:
                MultiCore.plugin.getLogger().warning(message);
                break;
            case DEBUG:
                if (debug) MultiCore.plugin.getLogger().info("[DEBUG] " + message);
                break;
        }
    }

    /**
     * 记录Throwable
     *
     * @param level 日志等级
     * @param t     Throwable
     */
    public static void log(LoggerLevel level, Throwable t) {
        StringWriter writer = new StringWriter();
        t.printStackTrace(new PrintWriter(writer));
        log(level, writer.toString());
    }

}
