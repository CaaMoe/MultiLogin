package `fun`.iiii.multilogin.velocity.main

import com.google.inject.Inject
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.proxy.config.PlayerInfoForwarding
import com.velocitypowered.proxy.config.VelocityConfiguration
import `fun`.iiii.multilogin.velocity.inject.VelocityInjector
import moe.caa.multilogin.api.logger.Level
import moe.caa.multilogin.api.logger.Logger
import moe.caa.multilogin.api.logger.error
import moe.caa.multilogin.api.plugin.EnvironmentException
import moe.caa.multilogin.api.plugin.EnvironmentalCheckResult
import moe.caa.multilogin.api.plugin.IPlugin
import moe.caa.multilogin.core.main.MultiCore
import net.kyori.adventure.audience.Audience
import org.incendo.cloud.CommandManager
import org.incendo.cloud.SenderMapper
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.velocity.VelocityCommandManager
import java.io.File
import java.nio.file.Path


class MultiLoginVelocity @Inject constructor(
    val server: ProxyServer,
    logger: org.slf4j.Logger,
    @DataDirectory dataDirectory: Path
) : IPlugin {
    lateinit var multiCore: MultiCore

    init {
        initLogger(logger)
    }

    @Subscribe
    fun onInitialize(event: ProxyInitializeEvent) {
        try {
            this.multiCore = MultiCore(this)
            this.multiCore.enable()

            VelocityInjector(this).inject()
        } catch (exception: EnvironmentException) {
            server.shutdown()
        } catch (throwable: Throwable) {
            error("Failed to initializing the plugin.", throwable)
            server.shutdown()
        }
    }

    @Subscribe
    fun onShutdown(event: ProxyShutdownEvent) {
        try {
            if (::multiCore.isInitialized) {
                multiCore.disable()
            }
        } catch (throwable: Throwable) {
            error("Failed to initializing the plugin.", throwable)
        } finally {
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

    override fun generateCommandManager(executionCoordinator: ExecutionCoordinator<Audience>): CommandManager<Audience> =
        VelocityCommandManager(
            server.pluginManager.getPlugin("multilogin").orElseThrow(),
            server,
            executionCoordinator,
            SenderMapper.create({ it }, { it as CommandSource })
        )

    override val dataFolder: File = dataDirectory.toFile()

    override fun isOnlineMode() = server.configuration.isOnlineMode

    override fun isProfileForwarding() = (server.configuration as VelocityConfiguration).playerInfoForwardingMode != PlayerInfoForwarding.NONE

}