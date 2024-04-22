package moe.caa.multilogin.core.auth.validate

import moe.caa.multilogin.core.auth.AuthenticationFailureResult
import moe.caa.multilogin.core.auth.AuthenticationHandler
import moe.caa.multilogin.core.auth.AuthenticationResult
import moe.caa.multilogin.core.main.MultiCore
import moe.caa.multilogin.core.util.logDebug
import net.kyori.adventure.text.Component

class LoginValidator(
    private val authenticationHandler: AuthenticationHandler
) {
    fun auth(validateProfile: ValidateProfile): AuthenticationResult {
        logDebug(
            "Start checking the player ${validateProfile.gameProfile.username}(${validateProfile.gameProfile.uuid}) coming in from ${validateProfile.service.serviceId}(${validateProfile.service.serviceName})"
        )


        return AuthenticationFailureResult(
            Component.text(
                "TODO(已完成外置登录, 但是还有一些其他的判断没有完成)"
            )
        )
    }

    private fun initialData(validateProfile: ValidateProfile) {
        MultiCore.instance.sqlHandler.tableHandler
    }
}