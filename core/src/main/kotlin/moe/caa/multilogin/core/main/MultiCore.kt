package moe.caa.multilogin.core.main

import moe.caa.multilogin.api.logger.Logger
import moe.caa.multilogin.api.logger.debug
import moe.caa.multilogin.api.logger.warn
import moe.caa.multilogin.api.plugin.IPlugin
import moe.caa.multilogin.core.command.CommandHandler
import moe.caa.multilogin.core.resource.builddata.getBuildData
import moe.caa.multilogin.core.resource.builddata.getBuildDataStr
import moe.caa.multilogin.core.resource.builddata.showWarning
import moe.caa.multilogin.core.resource.configuration.ConfigurationHandler
import moe.caa.multilogin.core.util.FormattedThreadFactory
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class MultiCore(val plugin: IPlugin) {
    val asyncExecute: ExecutorService = Executors.newFixedThreadPool(
        16,
        FormattedThreadFactory("MultiLogin Async #d") { Thread(it).apply { isDaemon = true } })
    val commandHandler = CommandHandler(this)
    val configurationHandler = ConfigurationHandler(this)

    fun enable() {
        checkEnvironment()

//        configurationHandler.init()
        commandHandler.init()
    }

    fun disable() {

    }

    private fun checkEnvironment() {
        if (showWarning) {
            warn("######################################################");
            warn("#   Warning, you are not using a stable version");
            warn("# and may have some very fatal errors!");
            warn("#");
            warn("#   Please download the latest stable version");
            warn("# from https://github.com/CaaMoe/MultiLogin/releases");
            warn("#");
            warn("#     Build By   : ${getBuildData("build_by")}")
            warn("#     Build Time : ${getBuildData("build_timestamp")}")
            warn("#     Version    : ${getBuildData("version")}")
            warn("######################################################");
            Logger.debug(true)
        }

        debug("Build data: ${getBuildDataStr()}")
    }
}