package moe.caa.multilogin.core.resource.configuration

import moe.caa.multilogin.core.resource.configuration.service.BaseService
import moe.caa.multilogin.core.resource.configuration.service.ServiceType
import moe.caa.multilogin.core.resource.configuration.service.UUIDGenerateType
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.hocon.HoconConfigurationLoader
import java.io.File

sealed interface IConfig {
    fun read(node: ConfigurationNode)
}

data object GeneralConfiguration : IConfig {
    var debug = false
        private set
    var checkUpdate = true
        private set
    var forceUseLogin = true
        private set
    var services: Map<Int, BaseService> = emptyMap()
        private set

    override fun read(node: ConfigurationNode) {
        debug = node.node("debug").getBoolean(false)
        checkUpdate = node.node("check_update").getBoolean(true)
        forceUseLogin = node.node("force_use_login").getBoolean(true)
    }

    fun readServices(serviceFolder: File) {
        val services: MutableMap<Int, BaseService> = HashMap()

        serviceFolder.mkdirs()
        serviceFolder.listFiles()?.filter {
            it.name.endsWith(".conf", true)
        }?.forEach {
            val configurationNode = HoconConfigurationLoader.builder().file(it).build().load()
            if (configurationNode.node("service_id").isNull) throw ReadConfigurationException("service_id is null.")
            val serviceId = configurationNode.node("service_id").int

            val serviceName = configurationNode.node("service_name").getString("Unnamed")
            val serviceType = configurationNode.node("service_type").get(ServiceType::class.java)
            val uuidGenerateType =
                configurationNode.node("uuid_generate_type").get(UUIDGenerateType::class.java, UUIDGenerateType.ONLINE)
            val whitelist = configurationNode.node("whitelist").getBoolean(true)

            val baseService: BaseService = when (serviceType) {
                ServiceType.OFFICIAL -> TODO()
                ServiceType.BLESSING_SKIN -> TODO()
                ServiceType.CUSTOM_YGGDRASIL -> TODO()
                ServiceType.FLOODGATE -> TODO()
                null -> throw ReadConfigurationException("service_type is null.")
            }
            if (services.containsKey(serviceId)) throw ReadConfigurationException("The same service id value $serviceId exists.")
        }
    }
}


data object NameSetting : IConfig {
    var allowRegular = "^[0-9a-zA-Z_]{3,16}\$"
        private set
    var autoRepeatCorrect = true
        private set
    var detectRenameCorrect = true
        private set

    override fun read(node: ConfigurationNode) {
        allowRegular = node.node("allow_regular").getString("^[0-9a-zA-Z_]{3,16}\$")
        autoRepeatCorrect = node.node("auto_repeat_correct").getBoolean(true)
        detectRenameCorrect = node.node("detect_rename_correct").getBoolean(true)
    }
}

data object Support : IConfig {
    var floodgate = true
        private set
    var skinsRestorer = true
        private set

    override fun read(node: ConfigurationNode) {
        floodgate = node.node("floodgate").getBoolean(true)
        skinsRestorer = node.node("skinsrestorer").getBoolean(true)
    }
}

data object Database : IConfig {
    override fun read(node: ConfigurationNode) {
        TODO("Not yet implemented")
    }
}