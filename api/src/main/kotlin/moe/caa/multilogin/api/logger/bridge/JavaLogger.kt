package moe.caa.multilogin.api.logger.bridge

import moe.caa.multilogin.api.logger.Level
import org.jetbrains.annotations.ApiStatus
import java.util.logging.Logger

/**
 * Java 控制台日志
 */
@ApiStatus.Internal
class JavaLogger(private val handler: Logger) : moe.caa.multilogin.api.logger.Logger {
    override fun log(level: Level, message: String?, throwable: Throwable?) {
        when (level) {
            Level.DEBUG -> handler.log(java.util.logging.Level.FINE, message, throwable)
            Level.INFO -> handler.log(java.util.logging.Level.INFO, message, throwable)
            Level.WARN -> handler.log(java.util.logging.Level.WARNING, message, throwable)
            Level.ERROR -> handler.log(java.util.logging.Level.SEVERE, message, throwable)
        }
    }
}
