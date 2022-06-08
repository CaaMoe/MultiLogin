package moe.caa.multilogin.api.logger.bridges;

import lombok.AllArgsConstructor;
import moe.caa.multilogin.api.logger.Level;

import java.util.logging.Logger;

/**
 * java.util.logging.Logger 日志程序桥接
 */
@AllArgsConstructor
public class JavaLoggerBridge extends BaseLoggerBridge {
    private final Logger HANDLER;

    @Override
    public void log(Level level, String message, Throwable throwable) {
//        if (level == Level.DEBUG) {
//            HANDLER.log(java.util.logging.Level.FINER, message, throwable);
//        } else
        if (level == Level.INFO) {
            HANDLER.log(java.util.logging.Level.INFO, message, throwable);
        } else if (level == Level.WARN) {
            HANDLER.log(java.util.logging.Level.WARNING, message, throwable);
        } else if (level == Level.ERROR) {
            HANDLER.log(java.util.logging.Level.SEVERE, message, throwable);
        }
    }
}
