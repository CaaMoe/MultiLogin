package moe.caa.multilogin.api.logger.bridge

import moe.caa.multilogin.api.logger.Level
import moe.caa.multilogin.api.logger.Logger
import org.jetbrains.annotations.ApiStatus.Internal


/**
 * 控制台日志
 */
@Internal
class ConsoleLogger : Logger {
    override fun log(level: Level, message: String?, throwable: Throwable?) {
        when (level) {
            Level.DEBUG -> {
                println("[DEBUG] $message")
                throwable?.printStackTrace(System.out)
            }

            Level.INFO -> {
                println("[INFO] $message")
                throwable?.printStackTrace(System.out)
            }

            Level.ERROR -> {
                println("[ERROR] $message")
                throwable?.printStackTrace(System.err)
            }

            Level.WARN -> {
                println("[WARN] $message")
                throwable?.printStackTrace(System.err)
            }
        }
    }
}