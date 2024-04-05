package `fun`.ksnb.multilogin.velocity.main

import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.PluginContainer
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import `fun`.ksnb.multilogin.velocity.command.MultiLoginVelocityCommandHandler
import moe.caa.multilogin.api.logger.Level
import moe.caa.multilogin.api.logger.Logger
import moe.caa.multilogin.api.logger.error
import moe.caa.multilogin.api.plugin.IPlugin
import moe.caa.multilogin.core.main.MultiCore
import java.io.File
import java.nio.file.Path

class MultiLoginVelocity @Inject constructor(
    val server: ProxyServer,
    logger: org.slf4j.Logger,
    @DataDirectory dataDirectory: Path
) : IPlugin {
    lateinit var pluginContainer: PluginContainer
    override val dataFolder: File = dataDirectory.toFile()

    private val multiLoginVelocityCommandHandler = MultiLoginVelocityCommandHandler(this)
    val multiCore = MultiCore(this)

    companion object {
        private const val PLUGIN_ID = "multilogin"
    }

    init {
        initLogger(logger)
    }

    @Subscribe
    fun onInitialize(event: ProxyInitializeEvent?) {
        try {
            this.pluginContainer = server.pluginManager.getPlugin(PLUGIN_ID).orElseThrow()

            this.multiCore.enable()
            this.multiLoginVelocityCommandHandler.init()

        } catch (throwable: Throwable) {
            error("Failed to initializing the plugin.", throwable)
            server.shutdown()
        }
    }


    private fun initLogger(logger: org.slf4j.Logger) {
        Logger.logger = object : Logger {
            override fun log(level: Level, message: String?, throwable: Throwable?) {
                when (level) {
                    Level.DEBUG -> logger.debug(message, throwable)
                    Level.INFO -> logger.info(message, throwable)
                    Level.WARN -> logger.warn(message, throwable)
                    Level.ERROR -> logger.error(message, throwable)
                }
            }
        }
    }
}