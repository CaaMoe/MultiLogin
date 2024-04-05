package moe.caa.multilogin.core.sql

import com.zaxxer.hikari.HikariDataSource
import moe.caa.multilogin.api.logger.bridge.ConsoleLogger.debug
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.StatementContext
import org.jetbrains.exposed.sql.statements.expandArgs
import org.jetbrains.exposed.sql.transactions.transaction

class SQLHandler {
    private lateinit var database: Database
    private lateinit var dataSource: HikariDataSource
    private lateinit var inGameProfileV3Table: InGameProfileV3Table
    private lateinit var userDataV3Table: UserDataV3Table

    fun init() {
        // todo data source
        inGameProfileV3Table = InGameProfileV3Table
        userDataV3Table = UserDataV3Table

        database = Database.connect(dataSource)

        transaction(database) {
            addLogger(SQLLogger)
            SchemaUtils.create(userDataV3Table, inGameProfileV3Table)
        }
    }

    fun close() {
        if (::dataSource.isInitialized) {
            dataSource.close()
        }
    }

    object SQLLogger : SqlLogger {
        override fun log(context: StatementContext, transaction: Transaction) {
            debug("SQL Executing: ${context.expandArgs(transaction)}")
        }
    }
}