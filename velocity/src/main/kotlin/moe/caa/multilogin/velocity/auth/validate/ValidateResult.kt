package moe.caa.multilogin.velocity.auth.validate

import net.kyori.adventure.text.Component

sealed interface ValidateResult {
    data object Pass : ValidateResult
    data class Failure(val reason: Component) : ValidateResult
}

