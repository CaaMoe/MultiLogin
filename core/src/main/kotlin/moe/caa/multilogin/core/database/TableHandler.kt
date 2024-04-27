package moe.caa.multilogin.core.database

import moe.caa.multilogin.api.profile.GameProfile
import moe.caa.multilogin.core.database.v4.*
import moe.caa.multilogin.core.util.logDebug
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.StatementContext
import org.jetbrains.exposed.sql.statements.expandArgs
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.transactionManager
import java.util.*

class TableHandler(
    private val database: Database
) {
    private fun <T> handle(statement: Transaction.() -> T): T = transaction(
        database.transactionManager.defaultIsolationLevel,
        database.transactionManager.defaultReadOnly,
        database, statement
    )

    fun init() {
        handle {
            addLogger(object : SqlLogger {
                override fun log(context: StatementContext, transaction: Transaction) {
                    logDebug("SQL Executing: ${context.expandArgs(transaction)}")
                }
            })
            SchemaUtils.create(ProfileDataTable, UserDataTable, CacheWhitelistDataTable)
        }
    }

    // 通过loginName在UserData表里面查曾经使用这个名字成功登录过游戏的玩家的service来源id
    fun findExpectYggdrasilAuthLoginServices(loginName: String) = handle {
        UserDataTable.select(UserDataTable.serviceId)
            .where { UserDataTable.loginName eq loginName }
            .withDistinct()
            .map { it[UserDataTable.serviceId] }
    }

    fun findUserDate(serviceId: Int, loginUUID: UUID): UserData? = TODO()
    fun findUserData(dataIndex: Int): UserData? = TODO()
    fun findProfile(dataIndex: Int): ProfileData? = TODO()
    fun findProfile(profileId: UUID): ProfileData? = TODO()
    fun findProfileIgnoreCase(username: String): List<ProfileData> = TODO()

    fun checkInGameNameAvailable(username: String): Boolean = TODO()
    fun checkInGameUuidAvailable(uuid: UUID): Boolean = TODO()
    // 校验擦车白名单
    fun checkCacheWhitelist(serviceId: Int, loginProfile: GameProfile): Boolean = TODO()
    // 删除和校验擦车白名单
    fun checkAndRemoveCacheWhitelist(serviceId: Int, loginProfile: GameProfile): Boolean = TODO()

    // 创建一个新的档案
    fun createNewProfile(profileData: ProfileData): ProfileData = TODO()
    // 创建一个新数据
    fun createNewUserData(userData: UserData): UserData = TODO()
}