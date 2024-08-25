package moe.caa.multilogin.velocity.config

import io.ktor.client.engine.*
import moe.caa.multilogin.velocity.config.service.BaseService
import moe.caa.multilogin.velocity.config.service.yggdrasil.BaseYggdrasilService
import moe.caa.multilogin.velocity.config.service.yggdrasil.BlessingSkinYggdrasilService
import moe.caa.multilogin.velocity.config.service.yggdrasil.BlessingSkinYggdrasilServiceSetting
import moe.caa.multilogin.velocity.config.service.yggdrasil.OfflineYggdrasilService
import moe.caa.multilogin.velocity.main.MultiLoginVelocity
import moe.caa.multilogin.velocity.util.saveDefaultResource
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.hocon.HoconConfigurationLoader
import java.io.File
import java.io.IOException

/**
 * 配置文件
 */
class ConfigHandler(
    private val plugin: MultiLoginVelocity
) {
    private val servicesDirectory: File = File(plugin.dataDirectory.toFile(), "services")
    private val examplesDirectory: File = File(plugin.dataDirectory.toFile(), "examples")

    var configResource: ConfigurationNode = HoconConfigurationLoader.builder().buildAndLoadString("")
        private set

    var debug: Boolean = false
        private set

    var serviceMap: Map<Int, BaseService> = emptyMap()
        private set

    /**
     * 重载消息文件
     */
    fun init() = reload()
    fun reload() {
        val file = saveDefaultResource(plugin.dataDirectory.toFile(), "config.conf", false)
        configResource = HoconConfigurationLoader.builder().file(file).build().load()

        exportExamples()
        readConfig()
        readServices();
    }


    private fun readConfig() {
        this.debug = configResource.node("debug").getBoolean(false)
    }

    private fun exportExamples(){
        examplesDirectory.deleteRecursively()
        servicesDirectory.mkdirs()

        saveDefaultResource(plugin.dataDirectory.toFile(), "examples/template_cn_full.conf", true)
    }

    private fun readServices() {
        val services = servicesDirectory.listFiles { f -> f.name.endsWith(".conf", true) }?.map { f ->
            try {
                readService(f)
            } catch (e: Exception){
                throw ServiceReadException("Failed to read service file ${f.absolutePath}", e);
            }
        }?.toList()?: emptyList()

        // 检查 service_id 重复
        services.groupBy { it.baseServiceSetting.serviceId }.filter { it.value.size > 1 }.forEach { listEntry ->
            throw ServiceReadException(
                "There are two or more services with the same id of ${listEntry.key}. they are ${
                    listEntry.value.joinToString(", ") {
                        it.resourceFile.absolutePath
                    }
                }"
            )
        }


        // 检查类型唯一
        services.groupBy { it::class.java }.filter { it.value.size > 1 }.forEach { listEntry ->
            when(listEntry.key) {
                OfflineYggdrasilService::class.java -> {
                    throw ServiceReadException(
                        "There are two or more services with the service_type is official, but this service_type can not be repeated. they are ${
                            listEntry.value.joinToString(", ") {
                                it.resourceFile.absolutePath
                            }
                        }"
                    )
                }
                // todo floodgate...
            }
        }

        // 塞进去
        serviceMap = HashMap<Int, BaseService>().apply {
            services.forEach { service ->
                this[service.baseServiceSetting.serviceId] = service
                plugin.logger.info("Add a authentication service with id is ${
                    service.baseServiceSetting.serviceId
                } and name is ${
                    service.baseServiceSetting.serviceName
                }.")
            }
        }


        if (services.isEmpty()) {
            plugin.logger.warn("The server has not added any authentication service, please check your config.")
        } else {
            plugin.logger.info("Added ${serviceMap.size} authentication services.")
        }
    }

    private fun readService(file: File): BaseService {
        val configurationNode = HoconConfigurationLoader.builder().file(file).build().load()

        val baseServiceSetting = lazy {
            BaseService.BaseServiceSetting(
                configurationNode.node("service_id").get(Int::class.java) ?: throw IllegalArgumentException("service_id undefined."),
                configurationNode.node("service_name").getString("Unnamed"),
                configurationNode.node("profile_uuid_generate_type").get(BaseService.ProfileUUIDGenerateType::class.java, BaseService.ProfileUUIDGenerateType.SERVICE),
                configurationNode.node("profile_name_generate_format").getString("{username}"),
                configurationNode.node("whitelist").getBoolean(false),
            )
        }

        val yggdrasilServiceSetting = lazy {
            BaseYggdrasilService.YggdrasilServiceSetting(
                configurationNode.node("yggdrasil_service_setting").node("prevent_proxy").getBoolean(true),
                configurationNode.node("yggdrasil_service_setting").node("timeout").getInt(7000),
                configurationNode.node("yggdrasil_service_setting").node("retry").getInt(0),
                configurationNode.node("yggdrasil_service_setting").node("delay_retry").getInt(0),
            )
        }

        val blessingSkinYggdrasilServiceSetting = lazy {
            BlessingSkinYggdrasilServiceSetting(
                configurationNode.node("blessing_skin_yggdrasil_service_setting").node("yggdrasil_api_root").get(String::class.java)?:throw IllegalArgumentException("yggdrasil_api_root undefined."),
            )
        }

        return when (ServiceType.valueOf((configurationNode.node("service_type").get(String::class.java)?:throw IllegalArgumentException("service_type undefined.")).uppercase())) {
            ServiceType.OFFICIAL -> return OfflineYggdrasilService(
                plugin, file, baseServiceSetting.value, yggdrasilServiceSetting.value
            )
            ServiceType.BLESSING_SKIN_YGGDRASIL -> return BlessingSkinYggdrasilService(
                plugin, file, baseServiceSetting.value, yggdrasilServiceSetting.value, blessingSkinYggdrasilServiceSetting.value
            )

            ServiceType.CUSTOM_YGGDRASIL -> return OfflineYggdrasilService(
                    plugin, file, baseServiceSetting.value, yggdrasilServiceSetting.value
                )
            ServiceType.FLOODGATE -> return OfflineYggdrasilService(
                plugin, file, baseServiceSetting.value, yggdrasilServiceSetting.value
            )
        }
    }

    private enum class ServiceType{
        OFFICIAL,
        BLESSING_SKIN_YGGDRASIL,
        CUSTOM_YGGDRASIL,
        FLOODGATE
    }

    class ServiceReadException(message: String, e: Throwable? = null) : IOException(message, e)
}