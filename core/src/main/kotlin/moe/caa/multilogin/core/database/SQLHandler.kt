package moe.caa.multilogin.core.database

import com.zaxxer.hikari.HikariDataSource
import moe.caa.multilogin.api.logger.logDebug
import moe.caa.multilogin.core.database.v4.InGameProfileV4
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.StatementContext
import org.jetbrains.exposed.sql.statements.expandArgs
import org.jetbrains.exposed.sql.transactions.transaction

class SQLHandler {
    private lateinit var database: Database
    private lateinit var dataSource: HikariDataSource
    private lateinit var inGameProfileV3Table: InGameProfileV4

    fun init() {
        // todo data source
        inGameProfileV3Table = InGameProfileV4

        database = Database.connect(dataSource)

        transaction(database) {
            addLogger(SQLLogger)
            SchemaUtils.create(inGameProfileV3Table)
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