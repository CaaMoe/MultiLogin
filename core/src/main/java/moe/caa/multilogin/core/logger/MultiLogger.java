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

public class MultiLogger {
    private final MultiCore core;
    private boolean debug = true;
    private Log4JCore log4JCore;

    public MultiLogger(MultiCore core) {
        this.core = core;
    }

    public void init() {

        try {
            debug = core.config.get("debug", Boolean.class, true);
            Log4JCore l4c = new Log4JCore(core);
            l4c.init();
//            防止提前赋值造成不可用
            log4JCore = l4c;
        } catch (Exception e) {
            log(LoggerLevel.ERROR, e);
            log(LoggerLevel.ERROR, LanguageKeys.LOGGER_FILE_ERROR.getMessage(core));
            debug = true;
        }
        if (debug) log(LoggerLevel.INFO, LanguageKeys.DEBUG_ENABLE.getMessage(core));
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

    private void logDefault(LoggerLevel level, String message, Throwable throwable) {
        switch (level) {
            case INFO:
                core.plugin.getLogger().log(Level.INFO, message, throwable);
                break;
            case ERROR:
                core.plugin.getLogger().log(Level.SEVERE, message, throwable);
                break;
            case WARN:
                core.plugin.getLogger().log(Level.WARNING, message, throwable);
                break;
            case DEBUG:
                if (debug)
                    core.plugin.getLogger().log(Level.INFO, "[DEBUG] " + message, throwable);
                break;
        }
    }

}
