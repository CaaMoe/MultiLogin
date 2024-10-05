package moe.caa.multilogin.velocity.config

import moe.caa.multilogin.velocity.main.MultiLoginVelocity
import moe.caa.multilogin.velocity.util.loadHoconConfigurationNode
import moe.caa.multilogin.velocity.util.saveDefaultResource
import org.spongepowered.configurate.hocon.HoconConfigurationLoader

class ConfigHandler(
    val plugin: MultiLoginVelocity
) {
    var configurationNode = HoconConfigurationLoader.builder().buildAndLoadString("")

    fun reload(){
        configurationNode = saveDefaultResource(plugin.dataDirectory, "config.conf").loadHoconConfigurationNode()

    }
}