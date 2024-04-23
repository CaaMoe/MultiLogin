package moe.caa.multilogin.core.database

import moe.caa.multilogin.core.database.v4.CacheWhitelistDataTable
import moe.caa.multilogin.core.database.v4.ProfileData
import moe.caa.multilogin.core.database.v4.ProfileDataTable
import moe.caa.multilogin.core.database.v4.UserDataTable
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

    fun findExpectYggdrasilAuthLoginServices(loginName: String) = handle {
        UserDataTable.select(UserDataTable.serviceId)
            .where { UserDataTable.loginName eq loginName }
            .withDistinct()
            .map { it[UserDataTable.serviceId] }
    }

    fun findProfile(serviceId: Int, loginUuid: UUID): ProfileData? {
        val profileId = UserDataTable.select(UserDataTable.linkToProfileId)
            .where {
                UserDataTable.serviceId eq serviceId
                UserDataTable.loginUuid eq loginUuid
            }.limit(1)
            .map { it[UserDataTable.linkToProfileId] }
            .getOrNull(0) ?: return null



        return ProfileData.find { ProfileDataTable.id eq profileId }.limit(1)
            .firstOrNull()
    }
}