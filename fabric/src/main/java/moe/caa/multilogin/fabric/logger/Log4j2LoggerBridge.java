package moe.caa.multilogin.fabric.logger;

import moe.caa.multilogin.api.logger.Level;
import moe.caa.multilogin.api.logger.LoggerProvider;
import moe.caa.multilogin.api.logger.bridges.BaseLoggerBridge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Log4j2 日志桥接程序
 */
public class Log4j2LoggerBridge extends BaseLoggerBridge {
    private final Logger logger;

    public Log4j2LoggerBridge(Logger logger) {
        this.logger = logger;
    }

    public static void register() {
        LoggerProvider.setLogger(new Log4j2LoggerBridge(LogManager.getLogger("multilogin")));
    }

    @Override
    public void log(Level level, String message, Throwable throwable) {
        if (level == Level.DEBUG) {
            logger.debug(message, throwable);
        } else if (level == Level.INFO) {
            logger.info(message, throwable);
        } else if (level == Level.WARN) {
            logger.warn(message, throwable);
        } else if (level == Level.ERROR) {
            logger.error(message, throwable);
        }
    }
}
