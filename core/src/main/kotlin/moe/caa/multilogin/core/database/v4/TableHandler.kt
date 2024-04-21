package moe.caa.multilogin.core.database.v4

import moe.caa.multilogin.core.util.logDebug
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.StatementContext
import org.jetbrains.exposed.sql.statements.expandArgs
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.transactionManager

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
}