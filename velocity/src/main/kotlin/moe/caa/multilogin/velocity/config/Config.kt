package moe.caa.multilogin.velocity.config

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
    var debug = false
    var serviceMap = emptyMap<Int, BaseService>()
    var profileNameSetting = ProfileNameSetting()
    var commandSetting = CommandSetting()
    var offlineAuthSetting = OfflineAuthSetting()


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
        this.profileNameSetting = ProfileNameSetting(
            configResource.node("profile_name_setting").node("auto_cutting").getBoolean(true),
            configResource.node("profile_name_setting").node("auto_increment").getBoolean(true),
            configResource.node("profile_name_setting").node("allowed_regular").getString("^[a-zA-Z0-9_]+\$").toRegex(),
        )
        this.commandSetting = CommandSetting(
            configResource.node("command_setting").node("confirm_await_second").getInt(15),
            configResource.node("command_setting").node("link_accept_confirm_await_second").getInt(60),
        )

        this.offlineAuthSetting = OfflineAuthSetting(
            configResource.node("offline_auth_setting").node("enable").getBoolean(false),
            configResource.node("offline_auth_setting").node("bind_hosts").getList(String::class.java, emptyList()),
            configResource.node("offline_auth_setting").node("bind_plank_server").getString(""),
            configResource.node("offline_auth_setting").node("auth_success_transfer").getString(""),
            OfflineAuthSetting.ChooseProfileNameFromHostSetting(
                configResource.node("offline_auth_setting").node("choose_profile_name_from_host_pattern").node("enable")
                    .getBoolean(false),
                configResource.node("offline_auth_setting").node("choose_profile_name_from_host_pattern").node("patterns")
                    .childrenList().map {
                        OfflineAuthSetting.ChooseProfileNameFromHostSetting.Pattern(
                            it.node("starts_with").getString(""),
                            it.node("ends_with").getString(""),
                        )
                    }
            )
        )
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
            plugin.logger.warn("The server has not added any authentication services, please check your configuration.")
        } else {
            plugin.logger.info("Added ${serviceMap.size} authentication services.")
        }
    }

    private fun readService(file: File): BaseService {
        val configurationNode = HoconConfigurationLoader.builder().file(file).build().load()

        val baseServiceSetting = lazy {
            BaseService.BaseServiceSetting(
                configurationNode.node("service_id").get(Int::class.java)
                    ?: throw IllegalArgumentException("service_id is undefined."),
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
                configurationNode.node("blessing_skin_yggdrasil_service_setting").node("yggdrasil_api_root")
                    .get(String::class.java) ?: throw IllegalArgumentException("yggdrasil_api_root is undefined."),
            )
        }

        return when (ServiceType.valueOf(
            (configurationNode.node("service_type").get(String::class.java)
                ?: throw IllegalArgumentException("service_type is undefined.")).uppercase()
        )) {
            ServiceType.OFFICIAL -> return OfflineYggdrasilService(
                file, baseServiceSetting.value, yggdrasilServiceSetting.value
            )
            ServiceType.BLESSING_SKIN_YGGDRASIL -> return BlessingSkinYggdrasilService(
                file, baseServiceSetting.value, yggdrasilServiceSetting.value, blessingSkinYggdrasilServiceSetting.value
            )

            ServiceType.CUSTOM_YGGDRASIL -> return OfflineYggdrasilService(
                file, baseServiceSetting.value, yggdrasilServiceSetting.value
                )
            ServiceType.FLOODGATE -> return OfflineYggdrasilService(
                file, baseServiceSetting.value, yggdrasilServiceSetting.value
            )
        }
    }

    private enum class ServiceType {
        OFFICIAL,
        BLESSING_SKIN_YGGDRASIL,
        CUSTOM_YGGDRASIL,
        FLOODGATE
    }

    class ServiceReadException(message: String, e: Throwable? = null) : IOException(message, e)
}

data class ProfileNameSetting(
    val autoCutting: Boolean = true,
    val autoIncrement: Boolean = true,
    val allowedRegular: Regex = "^[a-zA-Z0-9_]+\$".toRegex(),
)


data class CommandSetting(
    val confirmAwaitSecond: Int = 15,
    val linkAcceptConfirmAwaitSecond: Int = 60,
)

data class OfflineAuthSetting(
    val enable: Boolean = false,
    val bindHosts: List<String> = emptyList(),
    val bindPlankServer: String = "",
    val authSuccessTransfer: String = "",
    val chooseProfileNameFromHostSetting: ChooseProfileNameFromHostSetting = ChooseProfileNameFromHostSetting(),
) {
    init {
        if (enable) {
            if (bindHosts.isEmpty()) {
                throw IllegalArgumentException("bind_host is empty.")
            }
            if (bindPlankServer.isEmpty()) {
                throw IllegalArgumentException("bind_plank_server is undefined.")
            }
            if (authSuccessTransfer.isEmpty()) {
                throw IllegalArgumentException("auth_success_transfer is undefined.")
            }
        }
    }

    data class ChooseProfileNameFromHostSetting(
        val enable: Boolean = false,
        val patterns: List<Pattern> = emptyList(),
    ) {
        init {
            if (enable) {
                if (patterns.isEmpty()) {
                    throw IllegalArgumentException("patterns is empty.")
                }
            }
        }

        fun chooseNameOrNull(host: String?): String? {
            patterns.forEach { pattern ->
                pattern.chooseNameOrNull(host)?.let { return it }
            }
            return null
        }

        data class Pattern(
            val startsWith: String = "",
            val endsWith: String = ""
        ) {
            fun chooseNameOrNull(host: String?): String? {
                var result = host ?: return null

                val indexOfStart = result.indexOf(startsWith, ignoreCase = true)
                if (indexOfStart < 0) return null
                result = result.replaceRange(indexOfStart, indexOfStart + startsWith.length, "")

                val indexOfEnd = result.lastIndexOf(endsWith, ignoreCase = true)
                if (indexOfEnd < 0) return null
                result = result.replaceRange(indexOfEnd, indexOfEnd + endsWith.length, "")
                return result
            }
        }
    }
}