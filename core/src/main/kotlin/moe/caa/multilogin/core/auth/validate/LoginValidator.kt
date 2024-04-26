package moe.caa.multilogin.core.auth.validate

import moe.caa.multilogin.core.auth.AuthenticationHandler
import moe.caa.multilogin.core.auth.AuthenticationResult
import moe.caa.multilogin.core.util.logDebug
import java.util.function.BiFunction

class LoginValidator(
    private val authenticationHandler: AuthenticationHandler
) {
    private val checkList: List<BiFunction<ValidateProfile, ValidateAuthenticationSuccessResult, ValidateAuthenticationResult>> = listOf()

    fun auth(validateProfile: ValidateProfile): AuthenticationResult {
        logDebug(
            "Start checking the player ${validateProfile.gameProfile.username}(${validateProfile.gameProfile.uuid}) coming in from ${validateProfile.service.serviceId}(${validateProfile.service.serviceName})"
        )

        // 包装一个验证通过的结局
        var validateAuthenticationSuccessResult = ValidateAuthenticationSuccessResult(validateProfile.gameProfile, validateProfile.gameProfile)

        // 遍历检查列表
        for (function in checkList) {
            when(val authenticationResult = function.apply(validateProfile, validateAuthenticationSuccessResult)){
                // 失败直接返回
                is ValidateAuthenticationFailureResult -> return authenticationResult
                // 成功继续
                is ValidateAuthenticationSuccessResult -> validateAuthenticationSuccessResult = authenticationResult;
            }
        }
        // 全部验证通过返回
        return validateAuthenticationSuccessResult
    }
}