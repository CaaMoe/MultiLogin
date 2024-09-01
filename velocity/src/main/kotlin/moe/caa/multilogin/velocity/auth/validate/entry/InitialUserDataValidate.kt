package moe.caa.multilogin.velocity.auth.validate.entry

import moe.caa.multilogin.velocity.auth.validate.ValidateContext
import moe.caa.multilogin.velocity.auth.validate.ValidateResult
import moe.caa.multilogin.velocity.database.UserDataTableV3
import moe.caa.multilogin.velocity.main.MultiLoginVelocity
import org.jetbrains.exposed.sql.upsert

// 初始化 UserData 数据
//
// 功能: 插入新的数据
//      更新 username
class InitialUserDataValidate(
    val plugin: MultiLoginVelocity
) : Validate {
    override fun checkIn(validateContext: ValidateContext): ValidateResult {
        plugin.database.useDatabase {
            UserDataTableV3.upsert {
                it[serviceId] = validateContext.baseService.baseServiceSetting.serviceId
                it[onlineUUID] = validateContext.serviceGameProfile.uuid
                it[onlineName] = validateContext.serviceGameProfile.username
            }
        }
        return ValidateResult.Pass
    }
}