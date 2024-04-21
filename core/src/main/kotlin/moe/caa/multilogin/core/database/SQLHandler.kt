package moe.caa.multilogin.core.database

import com.zaxxer.hikari.HikariDataSource
import moe.caa.multilogin.core.database.v4.CacheWhitelistData
import moe.caa.multilogin.core.database.v4.ProfileData
import moe.caa.multilogin.core.database.v4.UserData
import moe.caa.multilogin.core.util.logDebug
import moe.caa.multilogin.core.util.logInfo
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.StatementContext
import org.jetbrains.exposed.sql.statements.expandArgs
import org.jetbrains.exposed.sql.transactions.transaction

class SQLHandler {
    private lateinit var database: Database
    private lateinit var dataSource: HikariDataSource

    fun init() {
        logInfo("Database Type: ${moe.caa.multilogin.core.resource.configuration.Database.sqlDriverType}")

        dataSource = HikariDataSource(moe.caa.multilogin.core.resource.configuration.Database.hikariConfig)
        database = Database.connect(dataSource)

        transaction(database) {
            addLogger(SQLLogger)

            SchemaUtils.create(UserData)
            SchemaUtils.create(ProfileData)
            SchemaUtils.create(CacheWhitelistData)
        }
    }

    fun close() {
        if (::dataSource.isInitialized) {
            dataSource.close()
        }
    }

    object SQLLogger : SqlLogger {
        override fun log(context: StatementContext, transaction: Transaction) {
            logDebug("SQL Executing: ${context.expandArgs(transaction)}")
        }
    }
}