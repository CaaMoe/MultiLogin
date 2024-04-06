package moe.caa.multilogin.core.resource.configuration

import moe.caa.multilogin.api.logger.Logger
import moe.caa.multilogin.api.logger.info
import moe.caa.multilogin.core.main.MultiCore
import moe.caa.multilogin.core.resource.*
import moe.caa.multilogin.core.resource.builddata.showWarning
import org.spongepowered.configurate.hocon.HoconConfigurationLoader
import java.io.File
import java.io.IOException

class ConfigurationHandler(private val multiCore: MultiCore) {
    var checkUpdate = false

    fun init() {
        reload()
    }

    fun reload() {
        getResource(EXAMPLES_LITTLE_SKIN, true)
        getResource(EXAMPLES_OFFICIAL, true)
        getResource(EXAMPLES_TEMPLATE_CN_FULL, true)
        getResource(EXAMPLES_FLOODGATE, true)

        val configurationNode = HoconConfigurationLoader.builder().file(getResource(ROOT_CONFIGURATION))
            .build().load()

        GeneralConfiguration.read(configurationNode)
        NameSetting.read(configurationNode.node("name_setting"))
        Support.read(configurationNode.node("support"))
        Database.read(configurationNode.node("database"))

        GeneralConfiguration.readServices(File(multiCore.plugin.dataFolder, "service"))

        if (showWarning || GeneralConfiguration.debug) {
            Logger.debug(GeneralConfiguration.debug)
        } else {
            Logger.debug(false)
        }
    }

    private fun getResource(resource: String, cover: Boolean = false): File {
        val file = File(multiCore.plugin.dataFolder, resource)
        val exist = file.exists()

        if (cover || !exist) {
            file.parentFile?.mkdirs()
            javaClass.getResourceAsStream("/$resource")?.use { input ->
                file.outputStream().use { output -> input.transferTo(output) }
            } ?: throw IOException("Failed processing resource ${resource}.")

            if (exist) {
                info("Cover: $resource")
            } else {
                info("Extract: $resource")
            }
        }

        return file;
    }
}