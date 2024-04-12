package moe.caa.multilogin.core.resource.configuration

import moe.caa.multilogin.api.logger.Logger
import moe.caa.multilogin.core.main.MultiCore
import moe.caa.multilogin.core.resource.*
import org.spongepowered.configurate.hocon.HoconConfigurationLoader
import java.io.File

class ConfigurationHandler(private val multiCore: MultiCore) {
    var checkUpdate = false

    fun init() {
        reload()
    }

    fun reload() {
        saveDefaultResource(EXAMPLES_LITTLE_SKIN, true)
        saveDefaultResource(EXAMPLES_OFFICIAL, true)
        saveDefaultResource(EXAMPLES_TEMPLATE_CN_FULL, true)
        saveDefaultResource(EXAMPLES_FLOODGATE, true)

        val configurationNode = HoconConfigurationLoader.builder().file(saveDefaultResource(ROOT_CONFIGURATION))
            .build().load()

        GeneralConfiguration.read(configurationNode)
        NameSetting.read(configurationNode.node("name_setting"))
        Support.read(configurationNode.node("support"))
        Database.read(configurationNode.node("database"))

        GeneralConfiguration.readServices(File(multiCore.plugin.dataFolder, "service"))

        applyConfiguration()
    }

    private fun applyConfiguration(){
        Logger.debug(GeneralConfiguration.debug)
    }
}