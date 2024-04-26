package moe.caa.multilogin.core.auth

import moe.caa.multilogin.core.auth.service.yggdrasil.LoginProfile
import moe.caa.multilogin.core.auth.service.yggdrasil.YggdrasilAuthenticationSuccessResult
import moe.caa.multilogin.core.auth.service.yggdrasil.YggdrasilAuthenticator
import moe.caa.multilogin.core.auth.validate.LoginValidator
import moe.caa.multilogin.core.auth.validate.ValidateProfile
import moe.caa.multilogin.core.main.MultiCore
import moe.caa.multilogin.core.util.logDebug
import moe.caa.multilogin.core.util.logInfo

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
            loginValidator.auth(ValidateProfile(yggdrasilAuthResult.service, yggdrasilAuthResult.gameProfile))

        if (finalResult is AuthenticationSuccessResult) {
            logInfo("${yggdrasilAuthResult.gameProfile.username}(uuid: ${yggdrasilAuthResult.gameProfile.uuid}) from authentication service ${yggdrasilAuthResult.service.serviceName}(sid: ${yggdrasilAuthResult.service.serviceId}) has been authenticated, profile redirected to ${finalResult.gameProfile.username}(uuid: ${finalResult.gameProfile.uuid}).")
        }
        return finalResult
    }
}