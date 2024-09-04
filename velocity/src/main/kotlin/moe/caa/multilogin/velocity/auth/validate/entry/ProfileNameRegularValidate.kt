package moe.caa.multilogin.velocity.auth.validate.entry

import moe.caa.multilogin.velocity.auth.validate.ValidateContext
import moe.caa.multilogin.velocity.auth.validate.ValidateResult
import moe.caa.multilogin.velocity.database.CacheWhitelistTableV2
import moe.caa.multilogin.velocity.database.UserDataTableV3
import moe.caa.multilogin.velocity.main.MultiLoginVelocity
import moe.caa.multilogin.velocity.message.replace
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

// 校验名称正则
class ProfileNameRegularValidate(
    val plugin: MultiLoginVelocity
) : Validate {
    override fun checkIn(validateContext: ValidateContext): ValidateResult {

        // 如果正则不对, failed
        if (!validateContext.profileGameProfile.username.matches(plugin.config.profileNameSetting.allowedRegular)) {
            return ValidateResult.Failure(
                plugin.message.message("validation_failure_reason_profile_name_not_match_regular")
                    .replace("{profile_name}", validateContext.profileGameProfile.username)
                    .replace("{allowed_regular}", plugin.config.profileNameSetting.allowedRegular)
            )
        }

        return ValidateResult.Pass
    }
}