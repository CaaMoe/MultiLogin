package moe.caa.multilogin.api.logger.bridge

import moe.caa.multilogin.api.logger.Level
import moe.caa.multilogin.api.logger.Logger
import org.jetbrains.annotations.ApiStatus

@ApiStatus.Internal
class DebugLogger(val originLogger: Logger): Logger {
    override fun log(level: Level, message: String?, throwable: Throwable?) {
        if (level == Level.DEBUG) {
            originLogger.log(Level.INFO, message, throwable)
        } else {
            originLogger.log(level, message, throwable)
        }
    }
}