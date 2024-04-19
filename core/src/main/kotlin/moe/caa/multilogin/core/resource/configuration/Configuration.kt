package moe.caa.multilogin.core.resource.configuration

import com.zaxxer.hikari.HikariConfig
import moe.caa.multilogin.core.resource.configuration.service.*
import moe.caa.multilogin.core.resource.configuration.service.yggdrasil.YggdrasilBlessingSkinService
import moe.caa.multilogin.core.resource.configuration.service.yggdrasil.YggdrasilCustomService
import moe.caa.multilogin.core.resource.configuration.service.yggdrasil.YggdrasilOfficialService
import moe.caa.multilogin.core.util.camelCaseToUnderscore
import moe.caa.multilogin.core.util.logInfo
import moe.caa.multilogin.core.util.logWarn
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.hocon.HoconConfigurationLoader
import java.io.File
import java.lang.reflect.Modifier
import java.util.*

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
            if (configurationNode.node("service_id").isNull) throw ReadConfigurationException("service_id in file ${it.absolutePath} is null.")
            val serviceId = configurationNode.node("service_id").int.apply {
                if (this > 127 || this < 0) throw ReadConfigurationException("service_id in file ${it.absolutePath} is out of bounds, The value can only be between 0 and 127.")
            }

            val serviceName = configurationNode.node("service_name").getString("Unnamed")
            val serviceType =
                configurationNode.node("service_type").get(ServiceType::class.java)
                    ?: throw ReadConfigurationException("service_type in file ${it.absolutePath} is null.")
            val uuidGenerateType =
                configurationNode.node("uuid_generate_type").get(UUIDGenerateType::class.java, UUIDGenerateType.ONLINE)
            val whitelist = configurationNode.node("whitelist").getBoolean(true)

            val baseService: BaseService = when (serviceType) {
                ServiceType.OFFICIAL -> YggdrasilOfficialService(
                    serviceId, serviceName, uuidGenerateType, whitelist,
                    configurationNode.node("yggdrasil_settings", "track_ip").getBoolean(true),
                    configurationNode.node("yggdrasil_settings", "timeout").getInt(10000),
                    configurationNode.node("yggdrasil_settings", "retry").getInt(0),
                    configurationNode.node("yggdrasil_settings", "delay_retry").getInt(0),
                )

                ServiceType.BLESSING_SKIN -> YggdrasilBlessingSkinService(
                    serviceId, serviceName, uuidGenerateType, whitelist,
                    configurationNode.node("yggdrasil_settings", "track_ip").getBoolean(true),
                    configurationNode.node("yggdrasil_settings", "timeout").getInt(10000),
                    configurationNode.node("yggdrasil_settings", "retry").getInt(0),
                    configurationNode.node("yggdrasil_settings", "delay_retry").getInt(0),
                    (configurationNode.node("yggdrasil_settings", "blessing_skin", "yggdrasil_api_root").string
                        ?: throw ReadConfigurationException("yggdrasil_api_root in file ${it.absolutePath} is null")).let { url ->
                        if (url.endsWith("/")) url.substring(0, url.length - 1) else url
                    }
                )

                ServiceType.CUSTOM_YGGDRASIL -> YggdrasilCustomService(
                    serviceId, serviceName, uuidGenerateType, whitelist,
                    configurationNode.node("yggdrasil_settings", "track_ip").getBoolean(true),
                    configurationNode.node("yggdrasil_settings", "timeout").getInt(10000),
                    configurationNode.node("yggdrasil_settings", "retry").getInt(0),
                    configurationNode.node("yggdrasil_settings", "delay_retry").getInt(0),
                    configurationNode.node("yggdrasil_settings", "custom", "http_method_type")
                        .get(HttpMethodType::class.java)
                        ?: throw ReadConfigurationException("http_method_type in file ${it.absolutePath} is null."),
                    configurationNode.node("yggdrasil_settings", "custom", "has_joined_url").string
                        ?: throw ReadConfigurationException("has_joined_url in file ${it.absolutePath} is null."),
                    configurationNode.node("yggdrasil_settings", "custom", "track_ip_content").string
                        ?: throw ReadConfigurationException("track_ip_content in file ${it.absolutePath} is null."),
                    configurationNode.node("yggdrasil_settings", "custom", "post_content").string
                        ?: throw ReadConfigurationException("post_content in file ${it.absolutePath} is null."),
                    )

                ServiceType.FLOODGATE -> FloodgateService(serviceId, serviceName, uuidGenerateType, whitelist)
            }

            if (!baseService.serviceType.allowedDuplicate()) {
                if (services.values.map { e -> e.serviceType }.any { e -> e == baseService.serviceType }) {
                    throw ReadConfigurationException("There can be only one service_type whose type is ${baseService.serviceType.name}.")
                }
            }

            if (services.containsKey(serviceId)) throw ReadConfigurationException("The same service id value $serviceId exists.")
            services[serviceId] = baseService;
        }
        this.services = Collections.unmodifiableMap(services)

        services.forEach { (k, v) ->
            logInfo("Add a service whose id $k, type ${v.serviceType}, and name ${v.serviceName}.")
        }

        if (services.isEmpty()) {
            logWarn("The server does not have any services added, this will prevent all players from logging into the server.")
        } else {
            logInfo("${services.size} service${if (services.size == 1) "" else "s"} have been added.")
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
    lateinit var sqlBackend: SQLBackend
    lateinit var hikariConfig: HikariConfig

    override fun read(node: ConfigurationNode) {
        synchronized(this) {
            if (!::sqlBackend.isInitialized) {
                this.sqlBackend = node.node("sql_backend").get(SQLBackend::class.java, SQLBackend.MYSQL)
            }
            if (!::hikariConfig.isInitialized) {
                this.hikariConfig = HikariConfig()

                hikariConfig.javaClass.methods
                    .filter { !Modifier.isStatic(it.modifiers) }
                    .filter { it.name.startsWith("set") }
                    .filter { it.parameters.size == 1 }
                    .forEach {
                        it.isAccessible = true
                        val key = it.name.substring(3).camelCaseToUnderscore()

                        if (node.hasChild(key)) {
                            val configurationNode = node.node(key)
                            when (it.parameters[0].type) {
                                Boolean::class.java -> {
                                    it.invoke(hikariConfig, configurationNode.boolean)
                                }

                                String::class.java -> {
                                    it.invoke(hikariConfig, configurationNode.string)
                                }

                                Long::class.java -> {
                                    it.invoke(hikariConfig, configurationNode.long)
                                }

                                Int::class.java -> {
                                    it.invoke(hikariConfig, configurationNode.int)
                                }
                            }
                        }
                    }
            }
        }
    }
}

enum class SQLBackend {
    H2,
    MARIADB,
    MYSQL,
    ORACLE,
    POSTGRES,
    SQLSERVER,
    SQLITE,
}