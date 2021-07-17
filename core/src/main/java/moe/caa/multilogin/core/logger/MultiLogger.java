package moe.caa.multilogin.core.logger;

import moe.caa.multilogin.core.language.LanguageKeys;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.util.FileUtil;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

public class MultiLogger {
    private static final String NEW_LINE = System.getProperty("line.separator");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss]");
    private static final File LOG_FILE = new File(MultiCore.plugin.getDataFolder(), "MultiLogin.log");
    private static FileWriter fileWriter;
    private static boolean health = false;
    private static Logger vanLogger = null;
    private static boolean debug = true;

    public static void init() {
        vanLogger = MultiCore.plugin.getLogger();
        try {
            debug = MultiCore.config.get("debug", Boolean.class, true);
            FileUtil.createNewFileOrFolder(LOG_FILE, false);
            fileWriter = new FileWriter(LOG_FILE, true);
            health = true;
        } catch (Exception e) {
            log(LoggerLevel.ERROR, e);
            log(LoggerLevel.ERROR, LanguageKeys.LOGGER_FILE_ERROR.getMessage());
            debug = true;
            health = false;
        }
        if (debug) log(LoggerLevel.INFO, LanguageKeys.DEBUG_ENABLE.getMessage());
    }

    public static void close() {
        try {
            fileWriter.flush();
            fileWriter.close();
        } catch (Exception ignore) {
        }
    }

    public static void log(LoggerLevel level, String message) {
        switch (level) {
            case INFO:
                vanLogger.info(message);
                break;
            case ERROR:
                vanLogger.severe(message);
                break;
            case WARN:
                vanLogger.warning(message);
                break;
            case DEBUG:
                if (debug) vanLogger.info("[DEBUG] " + message);
                break;
        }
        write(message, level);
    }

    public static void log(LoggerLevel level, Throwable t) {
        StringWriter writer = new StringWriter();
        t.printStackTrace(new PrintWriter(writer));
        log(level, writer.toString());
    }


    private static String getTag(LoggerLevel level) {
        return DATE_FORMAT.format(new Date()) + " [" + Thread.currentThread().getName() + "/" + level.name() + "]: ";
    }

    private static synchronized void write(String message, LoggerLevel level) {
        if (health) {
            try {
                fileWriter.write(getTag(level));
                fileWriter.write(message);
                fileWriter.write(NEW_LINE);
                fileWriter.flush();
            } catch (IOException ignored) {
            }
        }
    }
}
