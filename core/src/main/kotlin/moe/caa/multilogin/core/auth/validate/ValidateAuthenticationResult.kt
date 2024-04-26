package moe.caa.multilogin.core.auth.validate

import moe.caa.multilogin.api.profile.GameProfile
import moe.caa.multilogin.core.auth.AuthenticationFailureResult
import moe.caa.multilogin.core.auth.AuthenticationResult
import moe.caa.multilogin.core.auth.AuthenticationSuccessResult
import net.kyori.adventure.text.Component

sealed interface ValidateAuthenticationResult : AuthenticationResult

class ValidateAuthenticationFailureResult(failedReason: Component): AuthenticationFailureResult(failedReason), ValidateAuthenticationResult

class ValidateAuthenticationSuccessResult(
    val loginGameProfile: GameProfile,
    val inGameProfile: GameProfile
): AuthenticationSuccessResult(inGameProfile), ValidateAuthenticationResult

