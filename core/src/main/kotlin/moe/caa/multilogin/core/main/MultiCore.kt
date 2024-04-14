package moe.caa.multilogin.core.main

import moe.caa.multilogin.api.exception.BreakSignalException
import moe.caa.multilogin.api.logger.logWarn
import moe.caa.multilogin.api.plugin.IPlugin
import moe.caa.multilogin.core.auth.AuthenticationHandler
import moe.caa.multilogin.core.command.CommandHandler
import moe.caa.multilogin.core.database.SQLHandler
import moe.caa.multilogin.core.resource.builddata.getBuildData
import moe.caa.multilogin.core.resource.builddata.showWarning
import moe.caa.multilogin.core.resource.configuration.ConfigurationHandler
import moe.caa.multilogin.core.resource.message.MessageHandler
import moe.caa.multilogin.core.util.FormattedThreadFactory
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MultiCore(val plugin: IPlugin) {
    val asyncExecute: ExecutorService = Executors.newFixedThreadPool(
        16,
        FormattedThreadFactory("MultiLogin Async #d") { Thread(it).apply { isDaemon = true } })
    val commandHandler = CommandHandler(this)
    val configurationHandler = ConfigurationHandler(this)
    val messageHandler = MessageHandler()
    val sqlHandler = SQLHandler()
    val authenticationHandler = AuthenticationHandler(this)

    companion object {
        lateinit var instance: MultiCore
    }

    fun enable() {
        instance = this
        checkEnvironment()

        configurationHandler.init()
        commandHandler.init()
        messageHandler.init()
        sqlHandler.init()
    }

    fun disable() {
        sqlHandler.close()
    }

    private fun checkEnvironment() {
        if (!plugin.isOnlineMode()) {
            moe.caa.multilogin.api.logger.logError("Please enable online mode, otherwise the plugin will not work!!!")
            throw BreakSignalException("The server will be forced to shut down!!!")
        }
        if(!plugin.isProfileForwarding()){
            moe.caa.multilogin.api.logger.logError("Please enable profile forwarding, otherwise the plugin will not work!!!")
            throw BreakSignalException("The server will be forced to shut down!!!")
        }

        if (showWarning) {
            logWarn("######################################################");
            logWarn("#   Warning, you are not using a stable version");
            logWarn("# and may have some very fatal errors!");
            logWarn("#");
            logWarn("#   Please download the latest stable version");
            logWarn("# from https://github.com/CaaMoe/MultiLogin/releases");
            logWarn("#");
            logWarn("#     Build By   : ${getBuildData("build_by")}")
            logWarn("#     Build Time : ${getBuildData("build_timestamp")}")
            logWarn("#     Version    : ${getBuildData("version")}")
            logWarn("######################################################");
        }
    }

    fun reload() {
        configurationHandler.reload()
        messageHandler.reload()
    }
}