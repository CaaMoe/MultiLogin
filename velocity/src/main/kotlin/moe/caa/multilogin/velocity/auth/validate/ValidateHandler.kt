package moe.caa.multilogin.velocity.auth.validate

import moe.caa.multilogin.velocity.auth.validate.entry.InitialProfileDataValidate
import moe.caa.multilogin.velocity.auth.validate.entry.InitialUserDataValidate
import moe.caa.multilogin.velocity.auth.validate.entry.ProfileNameRegularValidate
import moe.caa.multilogin.velocity.auth.validate.entry.WhitelistValidate
import moe.caa.multilogin.velocity.main.MultiLoginVelocity

class ValidateHandler(
    val plugin: MultiLoginVelocity
) {
    private val validators = listOf(
        InitialUserDataValidate(plugin),
        WhitelistValidate(plugin),
        InitialProfileDataValidate(plugin),
        ProfileNameRegularValidate(plugin),
    )


    fun checkIn(validateContext: ValidateContext): ValidateResult {
        for (validate in validators) {
            when (val result = validate.checkIn(validateContext)) {
                is ValidateResult.Failure -> return result
                is ValidateResult.Pass -> {}
            }
        }
        return ValidateResult.Pass
    }
}