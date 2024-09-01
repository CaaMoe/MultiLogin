package moe.caa.multilogin.velocity.auth.validate.entry

import moe.caa.multilogin.velocity.auth.validate.ValidateContext
import moe.caa.multilogin.velocity.auth.validate.ValidateResult
import moe.caa.multilogin.velocity.database.CacheWhitelistTableV2
import moe.caa.multilogin.velocity.database.UserDataTableV3
import moe.caa.multilogin.velocity.main.MultiLoginVelocity
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList

// 校验白名单
class WhitelistValidate(
    val plugin: MultiLoginVelocity
) : Validate {
    override fun checkIn(validateContext: ValidateContext): ValidateResult {
        // 消耗白名单
        val findCacheWhitelists = plugin.database.useDatabase {
            CacheWhitelistTableV2.select(
                CacheWhitelistTableV2.id
            ).where {
                // 数据库名字小写 == 当前名字小写 and (数据库serviceId字段为空 or 数据库serviceId == 当前serviceId)
                (CacheWhitelistTableV2.target.lowerCase() eq validateContext.serviceGameProfile.username.lowercase()) and
                        (CacheWhitelistTableV2.serviceId.isNull() or (CacheWhitelistTableV2.serviceId eq validateContext.baseService.baseServiceSetting.serviceId))
            }.map {
                it[CacheWhitelistTableV2.id].value
            }
        }

        // 查询记录不为空
        if (findCacheWhitelists.isNotEmpty()) {
            plugin.database.useDatabase {
                // 更新白名单为 true
                UserDataTableV3.update({
                    UserDataTableV3.serviceId eq validateContext.baseService.baseServiceSetting.serviceId
                    UserDataTableV3.onlineUUID eq validateContext.serviceGameProfile.uuid
                }) {
                    it[whitelist] = true
                }

                // 然后删掉
                CacheWhitelistTableV2.deleteWhere { id inList findCacheWhitelists }
            }

            // 直接给过
            return ValidateResult.Pass
        }

        if (validateContext.baseService.baseServiceSetting.whitelist) {
            val whitelist = plugin.database.useDatabase {
                UserDataTableV3
                    .select(UserDataTableV3.whitelist)
                    .where {
                        UserDataTableV3.serviceId eq validateContext.baseService.baseServiceSetting.serviceId
                        UserDataTableV3.onlineUUID eq validateContext.serviceGameProfile.uuid
                    }.limit(1).map {
                        it[UserDataTableV3.whitelist]
                    }.firstOrNull() ?: false
            }
            // 如果没有白名单, 踢出去
            if (!whitelist) {
                return ValidateResult.Failure(plugin.message.message("validation_failure_reason_no_whitelist"))
            }
        }
        return ValidateResult.Pass
    }
}