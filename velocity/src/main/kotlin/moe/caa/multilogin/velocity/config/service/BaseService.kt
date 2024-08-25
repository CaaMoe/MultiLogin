package moe.caa.multilogin.velocity.config.service

import moe.caa.multilogin.api.profile.GameProfile
import moe.caa.multilogin.velocity.config.ConfigHandler
import moe.caa.multilogin.velocity.main.MultiLoginVelocity
import java.io.File
import java.util.*

/**
 * 表示一个 Service
 */
abstract class BaseService (
    val plugin: MultiLoginVelocity,
    val resourceFile: File,
    val baseServiceSetting: BaseServiceSetting
) {
    init {
        if(baseServiceSetting.serviceId < 0 || baseServiceSetting.serviceId > 127){
            throw ConfigHandler.ServiceReadException("service_id must be between 0 and 127")
        }
    }


    // 根据 service 结果生成新的 profile
    fun generateNewProfile(serviceResult: GameProfile) = GameProfile(
        baseServiceSetting.profileUUIDGenerateType.generateUUID(serviceResult),
        baseServiceSetting.profileNameGenerateFormat.replace("{name}", serviceResult.username),
        serviceResult.properties
    )

    // UUID 生成规则
    enum class ProfileUUIDGenerateType{
        SERVICE,
        OFFLINE,
        RANDOM;

        fun generateUUID(serviceResult: GameProfile): UUID = when(this){
                SERVICE -> serviceResult.uuid
                OFFLINE -> UUID.nameUUIDFromBytes("OfflinePlayer:${serviceResult.username}".toByteArray(Charsets.UTF_8));
                RANDOM -> UUID.randomUUID()
        }
    }

    data class BaseServiceSetting(
        // service 的 id
        val serviceId: Int,
        // service 的 name
        val serviceName: String,
        // uuid 生成规则
        val profileUUIDGenerateType: ProfileUUIDGenerateType,
        // username 生成规则
        val profileNameGenerateFormat: String,
        // 白名单
        val whitelist: Boolean
    )
}