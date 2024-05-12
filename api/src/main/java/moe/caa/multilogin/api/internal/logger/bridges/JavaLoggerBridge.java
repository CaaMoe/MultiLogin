package moe.caa.multilogin.api.internal.logger.bridges;

import lombok.AllArgsConstructor;
import moe.caa.multilogin.api.internal.logger.Level;
import org.jetbrains.annotations.ApiStatus;

import java.util.logging.Logger;

/**
 * java.util.logging.Logger 日志程序桥接
 */
@ApiStatus.Internal
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
