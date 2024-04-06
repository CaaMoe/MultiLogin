package moe.caa.multilogin.core.main

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonPrimitive
import moe.caa.multilogin.api.logger.Logger
import moe.caa.multilogin.api.logger.debug
import moe.caa.multilogin.api.logger.warn
import moe.caa.multilogin.api.plugin.IPlugin
import moe.caa.multilogin.core.command.CommandHandler
import moe.caa.multilogin.core.resource.builddata.buildDataElementObject
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

        configurationHandler.init()
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
            warn("#     Build By   : " + (buildDataElementObject["build_by"]?.jsonPrimitive?.content ?: "unknown"));
            warn("#     Build Time : " + (buildDataElementObject["build_timestamp"]?.jsonPrimitive?.content ?: "unknown"))
            warn("#     Version    : " + (buildDataElementObject["version"]?.jsonPrimitive?.content ?: "unknown"));
            warn("######################################################");
            Logger.debug(true)
        }

        debug("Build data is ${Json.encodeToString(buildDataElementObject)}")
    }
}