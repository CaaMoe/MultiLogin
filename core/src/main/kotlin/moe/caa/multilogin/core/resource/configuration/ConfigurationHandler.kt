package moe.caa.multilogin.core.resource.configuration

import moe.caa.multilogin.api.logger.LoggerProvider
import moe.caa.multilogin.core.main.MultiCore
import moe.caa.multilogin.core.resource.*
import moe.caa.multilogin.core.util.logError
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.hocon.HoconConfigurationLoader
import java.io.File

class ConfigurationHandler(private val multiCore: MultiCore) {
    var checkUpdate = false
    var configurationNode: ConfigurationNode? = null

    fun init() {
        try {
            reload()
        } catch (rce: ReadConfigurationException) {
            logError(rce.message)
            throw RuntimeException() // todo break
        }
    }

    fun reload() {
        saveDefaultResource(EXAMPLES_LITTLE_SKIN, true)
        saveDefaultResource(EXAMPLES_OFFICIAL, true)
        saveDefaultResource(EXAMPLES_TEMPLATE_CN_FULL, true)
        saveDefaultResource(EXAMPLES_FLOODGATE, true)

        HoconConfigurationLoader.builder().file(saveDefaultResource(ROOT_CONFIGURATION)).build().load().apply {
            configurationNode = this

            GeneralConfiguration.read(this)
            NameSetting.read(this.node("name_setting"))
            Support.read(this.node("support"))
        }

        GeneralConfiguration.readServices(File(multiCore.plugin.dataFolder, "service"))
        applyConfiguration()
    }

    private fun applyConfiguration(){
        LoggerProvider.debugMode = GeneralConfiguration.debug
    }
}