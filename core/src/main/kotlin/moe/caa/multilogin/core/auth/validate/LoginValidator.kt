package moe.caa.multilogin.core.auth.validate

import moe.caa.multilogin.core.auth.AuthenticationHandler
import moe.caa.multilogin.core.auth.AuthenticationResult
import moe.caa.multilogin.core.util.logDebug

class LoginValidator(
    private val authenticationHandler: AuthenticationHandler
) {
    fun auth(validateData: ValidateData): AuthenticationResult {
        logDebug(
            "Start checking the player ${validateData.loginProfile.username}(${validateData.loginProfile.uuid}) coming in from ${validateData.service.serviceId}(${validateData.service.serviceName})"
        )

        // 包装一个验证通过的结局
        var validateAuthenticationSuccessResult = ValidateAuthenticationSuccessResult(validateData.loginProfile)

        // debug remove
//        // 遍历检查列表
//        for (function in validateList) {
//            when(val authenticationResult = function.validate(validateData, validateAuthenticationSuccessResult)){
//                // 失败直接返回
//                is ValidateAuthenticationFailureResult -> return authenticationResult
//                // 成功继续
//                is ValidateAuthenticationSuccessResult -> validateAuthenticationSuccessResult = authenticationResult;
//            }
//        }
        // 全部验证通过返回
        return validateAuthenticationSuccessResult
    }
}