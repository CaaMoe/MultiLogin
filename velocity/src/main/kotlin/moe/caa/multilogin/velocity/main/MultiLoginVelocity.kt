package moe.caa.multilogin.velocity.main

import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyReloadEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.PluginContainer
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.proxy.VelocityServer
import com.velocitypowered.proxy.config.PlayerInfoForwarding
import moe.caa.multilogin.api.MultiLoginAPI
import moe.caa.multilogin.api.MultiLoginAPIProvider
import moe.caa.multilogin.velocity.config.ConfigHandler
import moe.caa.multilogin.velocity.database.DatabaseHandler
import moe.caa.multilogin.velocity.manager.VelocityLoginSourceDataManager
import moe.caa.multilogin.velocity.manager.VelocityProfileManager
import moe.caa.multilogin.velocity.manager.VelocityServiceManager
import moe.caa.multilogin.velocity.manager.VelocityUserManager
import org.slf4j.Logger
import java.nio.file.Path

@Plugin(id = "multilogin")
class MultiLoginVelocity @Inject constructor(
    server: ProxyServer,
    val logger: Logger,
    @DataDirectory val dataDirectory: Path
) : MultiLoginAPI {
    override val profileManager = VelocityProfileManager(this)
    override val serviceManager = VelocityServiceManager(this)
    override val userManager = VelocityUserManager(this)
    override val loginSourceDataManager = VelocityLoginSourceDataManager(this)

    val server: VelocityServer = server as VelocityServer

    lateinit var pluginContainer: PluginContainer
    val databaseHandler = DatabaseHandler(this)
    val configHandler = ConfigHandler(this)

    private var enabled = false

    init {
        MultiLoginAPIProvider.api = this
    }

    @Subscribe
    fun onEnable(event: ProxyInitializeEvent) {
        if(server.configuration.playerInfoForwardingMode == PlayerInfoForwarding.NONE){
            logger.error("""For account security, you should not set "player-info-forwarding-mode" to "none" in the configuration file "velocity.toml", please change it!!!""")
            logger.error("The server will be shut down.")
            server.shutdown()
            return
        }

        kotlin.runCatching {
            pluginContainer = server.pluginManager.ensurePluginContainer(this)
            configHandler.reload()

            databaseHandler.init()

        }.onSuccess {
            enabled = true
            logger.info("Loaded, using MultiLogin v${pluginContainer.description.version.get()} on ${server.version.name} - ${server.version.version}")
        }.onFailure {
            logger.error("An exception was encountered while loading the plugin.", it)
            logger.error("The server will be immediately shut down for the sake of account security!!!")
            server.shutdown()
        }
    }

    @Subscribe
    fun onDisable(event: ProxyShutdownEvent) {
        if (enabled) {
            kotlin.runCatching {
                databaseHandler.close()
            }.onSuccess {
                logger.info("Disabled.")
            }.onFailure {
                logger.error("An exception was encountered while close the plugin", it)
            }
        }
        server.shutdown()
    }

    @Subscribe
    fun onReload(event: ProxyReloadEvent) {
        configHandler.reload()
        logger.info("The resource file has been reloaded.")
    }
}