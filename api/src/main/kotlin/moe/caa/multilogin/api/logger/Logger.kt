package moe.caa.multilogin.api.logger

import moe.caa.multilogin.api.logger.bridge.ConsoleLogger
import moe.caa.multilogin.api.logger.bridge.DebugLogger
import org.jetbrains.annotations.ApiStatus.Internal

@Internal
interface Logger {
    companion object {
        var logger: Logger = ConsoleLogger()

        fun debug(debug: Boolean) {
            if (debug) {
                if (logger !is DebugLogger) {
                    logger = DebugLogger(logger)
                    logger.warn("Debug mode on.")
                }
            } else {
                if (logger is DebugLogger) {
                    logger = (logger as DebugLogger).originLogger
                    logger.warn("Debug mode off.")
                    debug(false)
                }
            }
        }
    }

    fun log(level: Level, message: String? = null, throwable: Throwable? = null)
    fun debug(message: String? = null, throwable: Throwable? = null) = log(Level.DEBUG, message, throwable)
    fun info(message: String? = null, throwable: Throwable? = null) = log(Level.INFO, message, throwable)
    fun warn(message: String? = null, throwable: Throwable? = null) = log(Level.WARN, message, throwable)
    fun error(message: String? = null, throwable: Throwable? = null) = log(Level.ERROR, message, throwable)
}

fun log(level: Level, message: String? = null, throwable: Throwable? = null) =
    Logger.logger.log(level, message, throwable)

fun logInfo(message: String? = null, throwable: Throwable? = null) = Logger.logger.info(message, throwable)
fun logDebug(message: String? = null, throwable: Throwable? = null) = Logger.logger.debug(message, throwable)
fun logWarn(message: String? = null, throwable: Throwable? = null) = Logger.logger.warn(message, throwable)
fun logError(message: String? = null, throwable: Throwable? = null) = Logger.logger.error(message, throwable)