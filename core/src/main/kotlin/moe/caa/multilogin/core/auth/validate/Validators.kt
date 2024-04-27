package moe.caa.multilogin.core.auth.validate

import moe.caa.multilogin.core.database.v4.ProfileData
import moe.caa.multilogin.core.database.v4.UserData
import moe.caa.multilogin.core.main.MultiCore
import moe.caa.multilogin.core.resource.configuration.GeneralConfiguration
import moe.caa.multilogin.core.resource.configuration.NameSetting
import moe.caa.multilogin.core.resource.message.language
import moe.caa.multilogin.core.util.incrementString
import moe.caa.multilogin.core.util.logError
import moe.caa.multilogin.core.util.logInfo
import moe.caa.multilogin.core.util.logWarn
import net.kyori.adventure.text.TextReplacementConfig
import java.util.*
import kotlin.properties.Delegates

val validateList: List<Validator> = listOf(
    InitialUserData
)

sealed interface Validator {
    fun validate(validateData: ValidateData, validateAuthenticationSuccessResult: ValidateAuthenticationSuccessResult): ValidateAuthenticationResult
}

// 初始化UserData和分配ProfileData
data object InitialUserData : Validator {
    override fun validate(
        validateData: ValidateData,
        validateAuthenticationSuccessResult: ValidateAuthenticationSuccessResult
    ): ValidateAuthenticationResult {
        val tableHandler = MultiCore.instance.sqlHandler.tableHandler
        var userData = tableHandler.findUserDate(
            validateData.service.serviceId,
            validateData.loginProfile.uuid
        )

        if(userData == null){
            if (validateData.service.whitelist) {
                if (!tableHandler.checkCacheWhitelist(validateData.service.serviceId, validateData.loginProfile)) {
                    return ValidateAuthenticationFailureResult(language("auth_validate_failed_no_whitelist"))
                }
            }

            synchronized(this){
                val expectProfileUuid = validateData.service.uuidGenerate.generateUUID(validateData.loginProfile.uuid, validateData.loginProfile.username)
                val expectUsername = validateData.loginProfile.username

                var changedProfileUuid = expectProfileUuid
                var changedUsername = expectUsername

                while (!tableHandler.checkInGameUuidAvailable(changedProfileUuid)) {
                    changedProfileUuid = UUID.randomUUID()
                }

                while (!tableHandler.checkInGameNameAvailable(changedUsername)){
                    if(NameSetting.autoRepeatCorrect){
                        changedUsername = expectUsername.incrementString()
                        validateData.nameAutoRepeatCorrected = true
                    } else {
                        return ValidateAuthenticationFailureResult(language("auth_validate_failed_name_repeat")
                                .replaceText(TextReplacementConfig.builder().matchLiteral("%current_username%").replacement(expectUsername).build())
                        )
                    }
                }

                if(!changedProfileUuid.equals(expectProfileUuid)){
                    logWarn("The uuid $expectProfileUuid in the new profile to be created is already in use and will be adjusted to a random value of $changedProfileUuid")
                }
                if(changedUsername != expectUsername){
                    logWarn("The username $expectUsername in the new profile to be created is already in use and will be adjusted to a correct value $changedUsername")
                }

                val initialProfile = tableHandler.createNewProfile(ProfileData(changedProfileUuid, changedUsername))
                logInfo("A new profile is created with the id is ${initialProfile.dataIndex} and profile uuid is $changedProfileUuid and profile name is $changedUsername")

                userData = tableHandler.createNewUserData(UserData(
                    validateData.service.serviceId,
                    validateData.loginProfile.uuid,
                    validateData.loginProfile.username,
                    false,
                    initialProfile.dataIndex,
                    initialProfile.dataIndex,
                ))
                logInfo("A new user data is created with the id is ${userData!!.dataIndex} and login service is ${validateData
                    .service.serviceId}(${validateData.service.serviceName}) and login name is ${validateData
                        .loginProfile.username} and login uuid is ${validateData.loginProfile.uuid} and initial profile id is ${initialProfile.dataIndex}")
            }
        }
        val profileData = tableHandler.findProfile(userData!!.linkToProfileId)
        if(profileData == null){
            logError("The profile whose id is ${userData!!.linkToProfileId} cannot be found, and data is suspected to be lost.(Trigger: $userData)")
            return ValidateAuthenticationFailureResult(language("auth_validate_failed_unknown_profile_index_id"))
        }

        validateData.userData = userData!!
        validateData.profileData = profileData
        return validateAuthenticationSuccessResult
    }
}