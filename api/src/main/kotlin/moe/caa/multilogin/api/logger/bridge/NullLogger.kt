package moe.caa.multilogin.api.logger.bridge

import moe.caa.multilogin.api.logger.Level
import moe.caa.multilogin.api.logger.Logger
import org.jetbrains.annotations.ApiStatus

/**
 * 空日志
 */
@ApiStatus.Internal
object NullLogger : Logger {
    override fun log(level: Level, message: String?, throwable: Throwable?) {}
}