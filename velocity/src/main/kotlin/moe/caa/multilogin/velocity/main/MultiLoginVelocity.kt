package moe.caa.multilogin.velocity.main

import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.proxy.VelocityServer
import com.velocitypowered.proxy.config.PlayerInfoForwarding
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.pool.HikariPool
import moe.caa.multilogin.velocity.auth.OnlineGameData
import moe.caa.multilogin.velocity.auth.validate.ValidateHandler
import moe.caa.multilogin.velocity.auth.yggdrasil.YggdrasilAuthenticationHandler
import moe.caa.multilogin.velocity.command.CommandHandler
import moe.caa.multilogin.velocity.config.ConfigHandler
import moe.caa.multilogin.velocity.database.DatabaseHandler
import moe.caa.multilogin.velocity.inject.VelocityServerChannelInitializerInjector
import moe.caa.multilogin.velocity.listener.PlayerLoginListener
import moe.caa.multilogin.velocity.message.Message
import moe.caa.multilogin.velocity.netty.handler.LoginEncryptionResponsePacketHandler
import moe.caa.multilogin.velocity.offline.OfflineLoginHandler
import moe.caa.multilogin.velocity.util.gameData
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.config.Configurator
import org.jetbrains.exposed.sql.exposedLogger
import org.slf4j.Logger
import java.nio.file.Path
import java.util.*

@Plugin(id = "multilogin")
class MultiLoginVelocity @Inject constructor(
    proxyServer: ProxyServer,
    val logger: Logger,
    @DataDirectory val dataDirectory: Path
) {
    val proxyServer = proxyServer as VelocityServer

    val message: Message = Message(this)
    val config: ConfigHandler = ConfigHandler(this)
    val database: DatabaseHandler = DatabaseHandler(this)
    val command = CommandHandler(this)

    val yggdrasilAuthenticationHandler = YggdrasilAuthenticationHandler(this)
    val validateAuthenticationHandler = ValidateHandler(this)

    fun reload() {
        message.reload()
        config.reload()
    }

    companion object {
        lateinit var instance: MultiLoginVelocity
    }

    init {
        instance = this

        // 鬼叫禁止...
        for (item in setOf(
            exposedLogger.name,
            HikariDataSource::class.java.name,
            HikariPool::class.java.name,
            HikariConfig::class.java.name,
        )) {
            Configurator.setLevel(item, Level.OFF)
        }
    }

    @Subscribe
    fun init(event: ProxyInitializeEvent) {
        try {

            if (!checkEnvironment()) {
                logger.warn("The proxy will be shut down.")
                proxyServer.shutdown()
                return
            }

            inject()

            PlayerLoginListener.init()
            OfflineLoginHandler.init()

            message.init()
            config.init()
            command.init()
            database.init(config.configResource.node("database", "data_source"))

            logger.info(
                "Loaded, using MultiLogin v${
                    proxyServer.pluginManager.getPlugin("multilogin").get().description.version.get()
                } on ${
                    proxyServer.version.name
                } - ${
                    proxyServer.version.version
                }."
            )
        }
        catch (throwable: Throwable) {
            logger.error("Failed to initialize plugin.", throwable)
            proxyServer.shutdown()
        }
    }

    @Subscribe
    fun disable(event: ProxyShutdownEvent) {
        LoginEncryptionResponsePacketHandler.close()
        database.close()
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
        LoginEncryptionResponsePacketHandler.Companion
    }

    fun logDebug(message: String?, t: Throwable? = null) = if (config.debug) {
        logger.info("[DEBUG] $message", t)
    } else {
        logger.debug(message, t)
    }


    fun findOnlineDataByUser(
        serviceId: Int,
        userUUID: UUID
    ): OnlineGameData? {
        proxyServer.allPlayers.forEach {
            val data = it.gameData
            if (data is OnlineGameData) {
                if (data.userProfile.uuid == userUUID) {
                    if (data.service.baseServiceSetting.serviceId == serviceId) {
                        return data
                    }
                }
            }
        }
        return null
    }
}