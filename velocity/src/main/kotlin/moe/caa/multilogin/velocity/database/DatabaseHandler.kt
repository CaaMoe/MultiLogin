package moe.caa.multilogin.velocity.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import moe.caa.multilogin.velocity.main.MultiLoginVelocity
import moe.caa.multilogin.velocity.util.camelCaseToUnderscore
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.StatementContext
import org.jetbrains.exposed.sql.statements.expandArgs
import org.jetbrains.exposed.sql.transactions.transaction
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Method
import java.lang.reflect.Modifier

class DatabaseHandler(
    private val plugin: MultiLoginVelocity
) {
    private lateinit var dataSource: HikariDataSource
    private lateinit var database: Database

    fun init(configurationNode: ConfigurationNode) {
        dataSource = HikariDataSource(readHikariConfig(configurationNode))
        database = Database.connect(dataSource)

        useDatabase {
            addLogger(object : SqlLogger {
                override fun log(context: StatementContext, transaction: Transaction) {
                    plugin.logDebug("SQLExecute: ${context.expandArgs(transaction)}")
                }
            })

            SchemaUtils.create(UserDataTableV3)
            SchemaUtils.create(ProfileTableV3)
            SchemaUtils.create(CacheWhitelistTableV2)
        }
    }

    fun <T> useDatabase(database: Database = this.database, statement: Transaction.() -> T): T = transaction(database) {
        statement.invoke(this)
    }


    fun close() {
        if (::dataSource.isInitialized) {
            dataSource.close()
        }
    }

    private fun readHikariConfig(configurationNode: ConfigurationNode) = HikariConfig().apply {
        val methodMap = mutableMapOf<String, Method>()
        javaClass.methods
            .filter { !Modifier.isStatic(it.modifiers) }
            .filter { Modifier.isPublic(it.modifiers) }
            .filter { it.name.startsWith("set") }
            .filter { it.parameters.size == 1 }
            .forEach {
                val key = it.name.substring(3).camelCaseToUnderscore()
                methodMap[key] = it
            }

        configurationNode.childrenMap().forEach { (key, child) ->
            val methodSetter = methodMap[key]
            if (methodSetter != null) {
                when (methodSetter.parameters[0].type) {
                    Boolean::class.java -> methodSetter.invoke(this, child.boolean)
                    Double::class.java -> methodSetter.invoke(this, child.double)
                    Float::class.java -> methodSetter.invoke(this, child.float)
                    Long::class.java -> methodSetter.invoke(this, child.long)
                    Int::class.java -> methodSetter.invoke(this, child.int)
                    String::class.java -> methodSetter.invoke(this, child.string)
                    else -> plugin.logger.warn(
                        "Unable to convert hikari config on path ${
                            child.path().array().toList().joinToString(separator = ".")
                        }(actual value: ${child.raw()})."
                    )
                }
            } else {
                plugin.logger.warn(
                    "Unknown hikari config on path ${
                        child.path().array().toList().joinToString(separator = ".")
                    }."
                )
            }
        }
    }
}