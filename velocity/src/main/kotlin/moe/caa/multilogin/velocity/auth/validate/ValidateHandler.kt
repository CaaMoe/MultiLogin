package moe.caa.multilogin.velocity.auth.validate

import moe.caa.multilogin.velocity.auth.validate.entry.InitialProfileDataValidate
import moe.caa.multilogin.velocity.auth.validate.entry.InitialUserDataValidate
import moe.caa.multilogin.velocity.auth.validate.entry.WhitelistValidate
import moe.caa.multilogin.velocity.main.MultiLoginVelocity

class ValidateHandler(
    val plugin: MultiLoginVelocity
) {
    private val validators = listOf(
        InitialUserDataValidate(plugin),
        WhitelistValidate(plugin),
        InitialProfileDataValidate(plugin),
    )


    fun checkIn(validateContext: ValidateContext): ValidateResult {
        for (validate in validators) {
            when (val result = validate.checkIn(validateContext)) {
                is ValidateResult.Failure -> return result
                is ValidateResult.Pass -> {}
            }
        }

        plugin.logger.info(
            "${validateContext.serviceGameProfile.username}(uuid: ${
                validateContext.serviceGameProfile.uuid
            }) from authentication service ${
                validateContext.baseService.baseServiceSetting.serviceName
            }(service id: ${
                validateContext.baseService.baseServiceSetting.serviceId
            }) has been authenticated, profile redirected to ${
                validateContext.resultGameProfile.username
            }(uuid: ${
                validateContext.resultGameProfile.uuid
            })"
        )
        return ValidateResult.Pass
    }
}