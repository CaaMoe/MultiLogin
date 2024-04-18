package moe.caa.multilogin.api.logger.bridge;

import moe.caa.multilogin.api.logger.Level;
import moe.caa.multilogin.api.logger.Logger;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class JavaLogger extends Logger {
    private final java.util.logging.Logger originalLogger;

    public JavaLogger(java.util.logging.Logger originalLogger) {
        this.originalLogger = originalLogger;
    }

    @Override
    public void handleLog(Level level, String message, Throwable throwable) {
        switch (level) {
            case DEBUG -> originalLogger.log(java.util.logging.Level.FINE, message, throwable);
            case INFO -> originalLogger.log(java.util.logging.Level.INFO, message, throwable);
            case WARN -> originalLogger.log(java.util.logging.Level.WARNING, message, throwable);
            case ERROR -> originalLogger.log(java.util.logging.Level.SEVERE, message, throwable);
            default -> originalLogger.log(java.util.logging.Level.SEVERE, "[UNKNOWN LEVEL]" + message, throwable);
        }
    }
}