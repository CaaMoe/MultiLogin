package moe.caa.multilogin.core.auth.validate

import moe.caa.multilogin.api.profile.GameProfile
import moe.caa.multilogin.core.database.v4.UnmodifiedProfileData
import moe.caa.multilogin.core.database.v4.UnmodifiedUserData
import moe.caa.multilogin.core.main.MultiCore
import moe.caa.multilogin.core.resource.configuration.NameSetting
import moe.caa.multilogin.core.resource.message.language
import moe.caa.multilogin.core.resource.message.replace
import moe.caa.multilogin.core.util.incrementString
import moe.caa.multilogin.core.util.logInfo
import moe.caa.multilogin.core.util.logWarn
import net.kyori.adventure.text.TextReplacementConfig
import java.util.*
import java.util.regex.Pattern

val validateList: List<Validator> = listOf(
    InitialDataValidator,
    WhitelistValidator,
    FinalValidator
)

sealed interface Validator {
    fun validate(validateData: ValidateData, validateAuthenticationSuccessResult: ValidateAuthenticationSuccessResult): ValidateAuthenticationResult
}

/**
 * 这个 Validator 负责从数据库中把 user data 和 link to profile 取出来
 *
 * 如果是个新玩家则会创建
 *
 * 包含: 新用户白名单校验(不完整)、auto_repeat_correct、allow_regular
 */
data object InitialDataValidator : Validator {
    override fun validate(
        validateData: ValidateData,
        validateAuthenticationSuccessResult: ValidateAuthenticationSuccessResult
    ): ValidateAuthenticationResult {
        val tableHandler = MultiCore.instance.sqlHandler.tableHandler
        var userData = tableHandler.findUserDate(
            validateData.service.serviceId,
            validateData.loginProfile.uuid
        )

        // 数据库中找不到它
        if(userData == null){
            // 当前 service 需要白名单
            if (validateData.service.whitelist) {
                // 检查没有白名单直接拒绝登录, 数据什么都不会保存
                if (!tableHandler.checkCacheWhitelist(validateData.service.serviceId, validateData.loginProfile)) {
                    return ValidateAuthenticationFailureResult(language("auth_validate_failed_no_whitelist"))
                }
            }

            synchronized(this){
                // 尝试枚举未被使用过的 profile uuid 和 profile name
                val expectProfileUuid = validateData.service.uuidGenerate.generateUUID(validateData.loginProfile.uuid, validateData.loginProfile.username)
                val expectProfileUsername = validateData.loginProfile.username

                var changedProfileUuid = expectProfileUuid
                var changedProfileUsername = expectProfileUsername

                // 如果 profile name 不可用
                while (!tableHandler.checkInGameNameAvailable(changedProfileUsername)){
                    // 接受 auto repeat correct
                    if(NameSetting.autoRepeatCorrect){
                        changedProfileUsername = expectProfileUsername.incrementString()
                        validateData.nameAutoRepeatCorrected = true
                    } else {
                        // 不接受直接拒绝登录, 数据什么都不会保存
                        return ValidateAuthenticationFailureResult(language("auth_validate_failed_name_repeat")
                                .replace("%current_username%", expectProfileUsername)
                        )
                    }
                }

                // 如果 profile uuid 不可用
                while (!tableHandler.checkInGameUuidAvailable(changedProfileUuid)) {
                    changedProfileUuid = UUID.randomUUID()
                }

                // profile name 不合法
                if (NameSetting.allowRegular.isNotEmpty()) {
                    if (!Pattern.matches(NameSetting.allowRegular, changedProfileUsername)) {
                        return ValidateAuthenticationFailureResult(language("auth_validate_failed_name_mismatch_regular")
                            .replace("%current_username%", expectProfileUsername)
                            .replace("%name_regular%", NameSetting.allowRegular)
                        )
                    }
                }

                if(!changedProfileUuid.equals(expectProfileUuid)){
                    logWarn("The uuid $expectProfileUuid in the new profile to be created is already in use and will be adjusted to a random value of $changedProfileUuid")
                }
                if(changedProfileUsername != expectProfileUsername){
                    logWarn("The username $expectProfileUsername in the new profile to be created is already in use and will be adjusted to a correct value $changedProfileUsername")
                }

                // 先创建 profile
                val initialProfile =
                    tableHandler.createNewProfile(UnmodifiedProfileData(changedProfileUuid, changedProfileUsername))
                logInfo("A new profile is created with the id is ${initialProfile.dataIndex} and profile uuid is $changedProfileUuid and profile name is $changedProfileUsername")

                // 然后创建 userdata, 记录 initial profile 为 link to profile
                userData = tableHandler.createNewUserData(
                    UnmodifiedUserData(
                        validateData.service,
                    validateData.loginProfile.uuid,
                    validateData.loginProfile.username,
                    false,
                        initialProfile,
                        initialProfile,
                ))
                logInfo("A new user data is created with the id is ${userData!!.dataIndex} and login service is ${validateData
                    .service.serviceId}(${validateData.service.serviceName}) and login name is ${validateData
                        .loginProfile.username} and login uuid is ${validateData.loginProfile.uuid} and initial profile id is ${initialProfile.dataIndex}")

                validateData.unmodifiedProfileData = initialProfile
            }
        }

        validateData.unmodifiedUserData = userData!!
        validateData.unmodifiedProfileData = userData!!.linkToProfileData
        return validateAuthenticationSuccessResult
    }
}

/**
 * 这个 Validator 负责校验白名单
 *
 */
data object WhitelistValidator: Validator {
    override fun validate(
        validateData: ValidateData,
        validateAuthenticationSuccessResult: ValidateAuthenticationSuccessResult
    ): ValidateAuthenticationResult {
        val tableHandler = MultiCore.instance.sqlHandler.tableHandler
        // 检查和删掉临时白名单
        if (tableHandler.checkAndRemoveCacheWhitelist(validateData.service.serviceId, validateData.loginProfile)) {
            if (!validateData.unmodifiedUserData!!.hasWhitelist()) {
                // 记录白名单
                validateData.unmodifiedUserData =
                    tableHandler.setWhitelist(validateData.unmodifiedUserData!!.dataIndex, true)
            }
        }
        if (validateData.service.whitelist) {
            // 检查白名单
            if (!validateData.unmodifiedUserData!!.hasWhitelist()) {
                return ValidateAuthenticationFailureResult(language("auth_validate_failed_no_whitelist"))
            }
        }

        return validateAuthenticationSuccessResult
    }
}

/**
 * 处理最终的登录请求
 */
data object FinalValidator: Validator {
    override fun validate(
        validateData: ValidateData,
        validateAuthenticationSuccessResult: ValidateAuthenticationSuccessResult
    ): ValidateAuthenticationResult {
        // todo 少了个异地登录检查，以后再加~
        val gameProfile = GameProfile(
            validateData.unmodifiedProfileData!!.profileUUID,
            validateData.unmodifiedProfileData!!.profileUsername,
            validateData.loginProfile.properties
        )
        return ValidateAuthenticationSuccessResult(gameProfile)
    }
}
