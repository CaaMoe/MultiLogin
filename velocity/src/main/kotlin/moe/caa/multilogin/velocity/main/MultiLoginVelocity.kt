package moe.caa.multilogin.velocity.main

import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.proxy.VelocityServer
import com.velocitypowered.proxy.config.PlayerInfoForwarding
import moe.caa.multilogin.velocity.auth.yggdrasil.YggdrasilAuthenticationHandler
import moe.caa.multilogin.velocity.command.CommandHandler
import moe.caa.multilogin.velocity.config.ConfigHandler
import moe.caa.multilogin.velocity.database.DatabaseHandler
import moe.caa.multilogin.velocity.inject.VelocityServerChannelInitializerInjector
import moe.caa.multilogin.velocity.message.Message
import moe.caa.multilogin.velocity.util.FormattedDaemonThreadFactory
import org.slf4j.Logger
import java.nio.file.Path
import java.util.concurrent.Executors

@Plugin(id = "multilogin")
class MultiLoginVelocity @Inject constructor(
    proxyServer: ProxyServer,
    val logger: Logger,
    @DataDirectory val dataDirectory: Path
) {
    val proxyServer = proxyServer as VelocityServer
    val asyncExecutor = Executors.newFixedThreadPool(16, FormattedDaemonThreadFactory("MultiLogin Async Executor #%d"))

    val message: Message = Message(this)
    val config: ConfigHandler = ConfigHandler(this,)
    val database: DatabaseHandler = DatabaseHandler(this)
    val command: CommandHandler = CommandHandler(this)

    val yggdrasilAuthenticationHandler: YggdrasilAuthenticationHandler = YggdrasilAuthenticationHandler(this)

    @Subscribe
    fun init(event: ProxyInitializeEvent) {
        try {
            if (!checkEnvironment()) {
                logger.warn("The proxy will be shut down.")
                proxyServer.shutdown()
                return
            }

            inject()

            message.init()
            config.init()
            command.init()
            database.init(config.configResource.node("database", "data_source"))
        }
        catch (throwable: Throwable) {
            logger.error("Failed to initialize plugin.", throwable)
            proxyServer.shutdown()
        }
    }

    private fun checkEnvironment(): Boolean {
        if (proxyServer.configuration.playerInfoForwardingMode == PlayerInfoForwarding.NONE) {
            logger.warn("For account security, you should not set \"player-info-forwarding-mode\" to \"none\" in the configuration file \"velocity.toml\", please change it!!!")
            return false
        }

        return true
    }

    private fun inject() {
        VelocityServerChannelInitializerInjector(this).inject()
    }

    fun logDebug(message: String) = if (config.debug) {
        logger.info("[DEBUG] $message")
    } else {
        logger.debug(message)
    }
}