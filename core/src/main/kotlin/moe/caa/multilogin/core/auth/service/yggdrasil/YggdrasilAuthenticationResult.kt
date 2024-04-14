package moe.caa.multilogin.core.auth.service.yggdrasil

import moe.caa.multilogin.api.profile.GameProfile
import moe.caa.multilogin.core.auth.AuthenticationFailureResult
import moe.caa.multilogin.core.auth.AuthenticationResult
import moe.caa.multilogin.core.auth.AuthenticationSuccessResult
import moe.caa.multilogin.core.resource.configuration.service.yggdrasil.YggdrasilService
import moe.caa.multilogin.core.resource.message.language
import net.kyori.adventure.text.Component

sealed interface YggdrasilAuthenticationResult : AuthenticationResult

class YggdrasilAuthenticationSuccessResult(
    val service: YggdrasilService,
    gameProfile: GameProfile,
) : AuthenticationSuccessResult(gameProfile), YggdrasilAuthenticationResult


class YggdrasilAuthenticationFailureResult private constructor(
    val failedReason: YggdrasilAuthenticationFailureReason,
    failureReason: Component
) : AuthenticationFailureResult(failureReason), YggdrasilAuthenticationResult {
    companion object {
        fun generate(reason: YggdrasilAuthenticationFailureReason) = YggdrasilAuthenticationFailureResult(
            reason, when (reason) {
                YggdrasilAuthenticationFailureReason.NO_YGGDRASIL_SERVICE -> language("auth_yggdrasil_failed_no_service")
                YggdrasilAuthenticationFailureReason.INVALID_SESSION -> language("auth_yggdrasil_failed_invalid_session")
                YggdrasilAuthenticationFailureReason.SERVER_BREAK_DOWN -> language("auth_yggdrasil_failed_server_break_down")
            }
        )
    }
}

enum class YggdrasilAuthenticationFailureReason(val severity: Int) {
    NO_YGGDRASIL_SERVICE(0),
    INVALID_SESSION(1),
    SERVER_BREAK_DOWN(2),
}