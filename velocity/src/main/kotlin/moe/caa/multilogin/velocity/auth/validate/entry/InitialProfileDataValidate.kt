package moe.caa.multilogin.velocity.auth.validate.entry

import moe.caa.multilogin.api.profile.GameProfile
import moe.caa.multilogin.velocity.auth.validate.ValidateContext
import moe.caa.multilogin.velocity.auth.validate.ValidateResult
import moe.caa.multilogin.velocity.database.ProfileTableV3
import moe.caa.multilogin.velocity.database.UserDataTableV3
import moe.caa.multilogin.velocity.main.MultiLoginVelocity
import moe.caa.multilogin.velocity.message.replace
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.upsert
import java.math.BigInteger
import java.sql.SQLIntegrityConstraintViolationException
import java.util.*

// 初始化 Profile Data
//
// 功能: 生成新的档案数据(处理 username 和 uuid 生成)
//
class InitialProfileDataValidate(
    val plugin: MultiLoginVelocity
) : Validate {
    override fun checkIn(validateContext: ValidateContext): ValidateResult {
        // 拿到 UserData 绑定的 profileUUID
        val profileUUID: UUID? = plugin.database.useDatabase {
            UserDataTableV3
                .select(UserDataTableV3.inGameProfileUUID)
                .where {
                    UserDataTableV3.serviceId eq validateContext.baseService.baseServiceSetting.serviceId
                    UserDataTableV3.onlineUUID eq validateContext.serviceGameProfile.uuid
                }.limit(1).map {
                    it[UserDataTableV3.inGameProfileUUID]
                }.firstOrNull()
        }

        // 如果, 对应的 profile 不存在, 就创建一个档案出来
        if (profileUUID == null) {
            var generateProfile: GameProfile =
                validateContext.baseService.generateNewProfile(validateContext.serviceGameProfile)

            // 如果名字太长
            if (generateProfile.username.length > 16) {
                // 开启了auto cutting, 截短
                if (plugin.config.profileNameSetting.autoCutting) {
                    generateProfile = generateProfile.withName(generateProfile.username.limitLength(16))
                } else {
                    // 否则直接失败
                    return ValidateResult.Failure(
                        plugin.message.message("validation_failure_reason_profile_name_is_too_long")
                            .replace("{profile_name}", generateProfile.username)
                    )
                }
            }

            // 把档案插入到数据库中
            while (
                plugin.database.useDatabase {
                    try {
                        ProfileTableV3.insert {
                            it[id] = generateProfile.uuid
                            it[currentUserNameOriginal] = generateProfile.username
                            it[currentUserNameLowerCase] = generateProfile.username.lowercase()
                        }
                        // 插入成功中断循环
                        return@useDatabase false
                    } catch (_: SQLIntegrityConstraintViolationException) {
                    }
                    // insert 失败, 走下面的随机名字
                    return@useDatabase true
                }
            ) {
                // 插入失败继续循环

                // 记录老名字
                val oldName = generateProfile.username
                // 随机没有被占用的uuid
                generateProfile = generateProfile.withUUID(randomProfileUUIDIfOccupy(generateProfile.uuid))
                // 随机没有被占用的name
                generateProfile = generateProfile.withName(incrementProfileNameIfOccupy(generateProfile.username))

                // 如果新名和老名不同
                if (oldName != generateProfile.username) {
                    // 没有打开 auto increment
                    if (!plugin.config.profileNameSetting.autoIncrement) {
                        // 中断
                        return ValidateResult.Failure(
                            plugin.message.message("validation_failure_reason_profile_name_occupy")
                                .replace("{profile_name}", oldName)
                        )
                    }
                }
            }

            // pass
            // 档案生成完毕后直接使用
            validateContext.resultGameProfile = generateProfile
            return ValidateResult.Pass
        } else {
            // profileID存在
            // 看看表中有没有记录
            val databaseProfile = plugin.database.useDatabase {
                ProfileTableV3.select(ProfileTableV3.currentUserNameOriginal).where {
                    ProfileTableV3.id eq profileUUID
                }.limit(1).map {
                    validateContext.serviceGameProfile
                        .withUUID(profileUUID)
                        .withName(it[ProfileTableV3.currentUserNameOriginal])
                }.firstOrNull()
            }

            // 表中有记录, 不管了
            if (databaseProfile != null) {
                validateContext.resultGameProfile = databaseProfile
                return ValidateResult.Pass
            }

            // 如果数据库中profile不存在，新建一个profile
            var generateProfile = validateContext.baseService.generateNewProfile(validateContext.serviceGameProfile)
            // 指定 profile UUID
            generateProfile = generateProfile.withUUID(profileUUID)

            // 和上面的一样
            if (generateProfile.username.length > 16) {
                if (plugin.config.profileNameSetting.autoCutting) {
                    generateProfile = generateProfile.withName(generateProfile.username.limitLength(16))
                } else {
                    return ValidateResult.Failure(
                        plugin.message.message("validation_failure_reason_profile_name_is_too_long")
                            .replace("{profile_name}", generateProfile.username)
                    )
                }
            }

            while (
                plugin.database.useDatabase {
                    try {
                        ProfileTableV3.upsert {
                            it[id] = generateProfile.uuid
                            it[currentUserNameOriginal] = generateProfile.username
                            it[currentUserNameLowerCase] = generateProfile.username.lowercase()
                        }
                        // 更新名字
                        return@useDatabase false
                    } catch (_: SQLIntegrityConstraintViolationException) {
                    }
                    // upsert 失败, 随机
                    return@useDatabase true
                }
            ) {
                val oldName = generateProfile.username
                generateProfile = generateProfile.withName(incrementProfileNameIfOccupy(generateProfile.username))

                // 和上面的一样
                if (oldName != generateProfile.username) {
                    if (!plugin.config.profileNameSetting.autoIncrement) {
                        return ValidateResult.Failure(
                            plugin.message.message("validation_failure_reason_profile_name_occupy")
                                .replace("{profile_name}", oldName)
                        )
                    }
                }
            }

            return ValidateResult.Pass
        }
    }

    private fun incrementProfileNameIfOccupy(username: String): String {
        var newName = username
        plugin.database.useDatabase {
            // 如果名字重复了, 换一个
            while (ProfileTableV3.select(ProfileTableV3.currentUserNameLowerCase).where {
                    ProfileTableV3.currentUserNameLowerCase eq newName.lowercase()
                }.map {
                    it[ProfileTableV3.currentUserNameLowerCase]
                }.isNotEmpty()) {

                newName = newName.incrementString()
            }
        }
        return newName
    }

    private fun randomProfileUUIDIfOccupy(uuid: UUID): UUID {
        var newUUID = uuid

        plugin.database.useDatabase {
            // 如果uuid重复了, 换一个
            while (ProfileTableV3.select(ProfileTableV3.id).where {
                    ProfileTableV3.id eq uuid
                }.map {
                    it[ProfileTableV3.id]
                }.isNotEmpty()) {

                newUUID = UUID.randomUUID()
            }
        }
        return newUUID
    }
}


private fun String.limitLength(maxLength: Int): String {
    if (length > maxLength) {
        return substring(0, maxLength)
    }
    return this
}

private fun String.incrementString(maxLength: Int = 16): String {
    // 计算尾部数字空间
    var numberSpec = StringBuilder()
    for (item in this.toCharArray().reversed()) {
        if (Character.isDigit(item)) {
            numberSpec.insert(0, item)
        }
    }
    // 剩下的名字空间
    val nameSpec = substring(0, length - numberSpec.length)

    // 如果没有数字, 就往后添 0
    if (numberSpec.isEmpty()) numberSpec.append("0")

    // 给数字 +1
    numberSpec = StringBuilder((numberSpec.toString().toBigInteger().add(BigInteger.ONE)).toString())

    // 数字空间过长，去头
    if (numberSpec.length > maxLength) {
        return numberSpec.substring(numberSpec.length - maxLength, numberSpec.length)
    }

    // 去中
    return nameSpec.substring(0, (maxLength - numberSpec.length).coerceAtMost(nameSpec.length)) + numberSpec
}