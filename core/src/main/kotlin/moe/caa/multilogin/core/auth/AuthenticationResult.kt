package moe.caa.multilogin.core.auth

import moe.caa.multilogin.api.profile.GameProfile
import net.kyori.adventure.text.Component

interface AuthenticationResult

open class AuthenticationSuccessResult(
    val gameProfile: GameProfile
) : AuthenticationResult

open class AuthenticationFailureResult(
    val failureReason: Component
) : AuthenticationResult