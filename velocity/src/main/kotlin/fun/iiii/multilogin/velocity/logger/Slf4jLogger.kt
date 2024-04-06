package `fun`.iiii.multilogin.velocity.logger

import moe.caa.multilogin.api.logger.Level
import moe.caa.multilogin.api.logger.Logger

class Slf4jLogger(private val logger: org.slf4j.Logger) : Logger {
    override fun log(level: Level, message: String?, throwable: Throwable?) {
        when (level) {
            Level.DEBUG -> logger.debug(message, throwable)
            Level.INFO -> logger.info(message, throwable)
            Level.WARN -> logger.warn(message, throwable)
            Level.ERROR -> logger.error(message, throwable)
        }
    }
}