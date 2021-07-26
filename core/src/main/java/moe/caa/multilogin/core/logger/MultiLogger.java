/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * moe.caa.multilogin.core.logger.MultiLogger
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package moe.caa.multilogin.core.logger;

import moe.caa.multilogin.core.language.LanguageKeys;
import moe.caa.multilogin.core.main.MultiCore;

import java.util.logging.Level;
import java.util.logging.Logger;

public class MultiLogger {
    private boolean debug = true;
    private Log4JCore log4JCore;
    private boolean log4jMode = false;

    public MultiLogger() {
    }

    public void init() {

        try {
            debug = MultiCore.getConfig().get("debug", Boolean.class, true);
            Log4JCore l4c = new Log4JCore();
            l4c.init();
//            防止提前赋值造成不可用
            log4JCore = l4c;
            if (MultiCore.getPlugin().getLogger() == null) {
                if (MultiCore.getPlugin().getLogger4J() != null) log4jMode = true;
            }
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
    public void log(LoggerLevel level, Throwable t) {
        log(level, "Throwable", t);
    }

    public void log(LoggerLevel level, String message) {
        log(level, message, null);
    }

    public void log(LoggerLevel level, String message, Throwable throwable) {
        logDefault(level, message, throwable);
        if (log4JCore != null)
            log4JCore.log(level, message, throwable);
    }

    public void logDirect(LoggerLevel level, String message, Throwable throwable) {
        if (log4JCore != null)
            log4JCore.log(level, message, throwable);
    }

    private void logDefault(LoggerLevel level, String message, Throwable throwable) {
        if (log4jMode) {
            _logDefault4J(level, message, throwable);
        } else {
            _logDefault(level, message, throwable);
        }
    }

    private void _logDefault(LoggerLevel level, String message, Throwable throwable) {
        Logger coreLogger = MultiCore.getPlugin().getLogger();
        if (coreLogger == null) {
            if (throwable != null) throwable.printStackTrace();
            System.out.println(level + " " + message);
            return;
        }
        switch (level) {
            case INFO:
                coreLogger.log(Level.INFO, message, throwable);
                break;
            case ERROR:
                coreLogger.log(Level.SEVERE, message, throwable);
                break;
            case WARN:
                coreLogger.log(Level.WARNING, message, throwable);
                break;
            case DEBUG:
                if (debug)
                    coreLogger.log(Level.INFO, "[DEBUG] " + message, throwable);
                break;
        }
    }

    private void _logDefault4J(LoggerLevel level, String message, Throwable throwable) {
        org.slf4j.Logger coreLogger = MultiCore.getPlugin().getLogger4J();
        if (coreLogger == null) {
            if (throwable != null) throwable.printStackTrace();
            System.out.println(level + " " + message);
            return;
        }
        switch (level) {
            case INFO:
                coreLogger.info(message, throwable);
                break;
            case ERROR:
                coreLogger.error(message, throwable);
                break;
            case WARN:
                coreLogger.warn(message, throwable);
                break;
            case DEBUG:
                if (debug)
                    coreLogger.info("[DEBUG] " + message, throwable);
                break;
        }
    }

    public boolean isDebug() {
        return debug;
    }
}
