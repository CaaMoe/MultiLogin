package moe.caa.multilogin.core.auth.validate

import moe.caa.multilogin.api.profile.GameProfile
import moe.caa.multilogin.core.auth.AuthenticationFailureResult
import moe.caa.multilogin.core.auth.AuthenticationHandler
import moe.caa.multilogin.core.auth.AuthenticationResult
import moe.caa.multilogin.core.resource.configuration.service.BaseService
import net.kyori.adventure.text.Component

class LoginValidator(
    private val authenticationHandler: AuthenticationHandler
) {
    fun auth(service: BaseService, gameProfile: GameProfile): AuthenticationResult {

        return AuthenticationFailureResult(
            Component.text(
                "TODO(已完成外置登录, 但是还有一些其他的判断没有完成)"
            )
        )
    }
}