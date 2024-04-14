package moe.caa.multilogin.core.auth

import moe.caa.multilogin.api.auth.LoginProfile
import moe.caa.multilogin.api.logger.logDebug
import moe.caa.multilogin.api.logger.logInfo
import moe.caa.multilogin.core.auth.service.yggdrasil.YggdrasilAuthenticationSuccessResult
import moe.caa.multilogin.core.auth.service.yggdrasil.YggdrasilAuthenticator
import moe.caa.multilogin.core.auth.validate.LoginValidator
import moe.caa.multilogin.core.main.MultiCore

class AuthenticationHandler(
    val multiCore: MultiCore
) {
    private val yggdrasilAuthenticator = YggdrasilAuthenticator(this)
    private val loginValidator = LoginValidator(this)

    fun auth(loginProfile: LoginProfile): AuthenticationResult {
        logDebug("Starting authenticating the login profile: $loginProfile")

        val yggdrasilAuthResult = yggdrasilAuthenticator.auth(loginProfile)

        if (yggdrasilAuthResult !is YggdrasilAuthenticationSuccessResult) return yggdrasilAuthResult
        val finalResult: AuthenticationResult =
            loginValidator.auth(yggdrasilAuthResult.service, yggdrasilAuthResult.gameProfile)

        if (finalResult is AuthenticationSuccessResult) {
            logInfo("${yggdrasilAuthResult.gameProfile.name}(uuid: ${yggdrasilAuthResult.gameProfile.uuid}) from authentication service ${yggdrasilAuthResult.service.serviceName}(sid: ${yggdrasilAuthResult.service.serviceId}) has been authenticated, profile redirected to ${finalResult.gameProfile.name}(uuid: ${finalResult.gameProfile.uuid}).")
        }
        return finalResult
    }
}