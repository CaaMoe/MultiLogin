package moe.caa.multilogin.velocity.auth.validate.entry

import moe.caa.multilogin.velocity.auth.validate.ValidateContext
import moe.caa.multilogin.velocity.auth.validate.ValidateResult
import moe.caa.multilogin.velocity.database.UserDataTableV3
import moe.caa.multilogin.velocity.main.MultiLoginVelocity
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.update

// 初始化 UserData 数据
//
// 功能: 插入新的数据
//      更新 username
class InitialUserDataValidate(
    val plugin: MultiLoginVelocity
) : Validate {
    override fun checkIn(validateContext: ValidateContext): ValidateResult {
        plugin.database.useDatabase {
            UserDataTableV3.insertIgnore {
                it[serviceId] = validateContext.baseService.baseServiceSetting.serviceId
                it[onlineUUID] = validateContext.userGameProfile.uuid
                it[onlineName] = validateContext.userGameProfile.username
            }

            UserDataTableV3.update({
                UserDataTableV3.serviceId eq validateContext.baseService.baseServiceSetting.serviceId
                UserDataTableV3.onlineUUID eq validateContext.userGameProfile.uuid
            }) {
                it[onlineName] = validateContext.userGameProfile.username
            }
        }
        return ValidateResult.Pass
    }
}