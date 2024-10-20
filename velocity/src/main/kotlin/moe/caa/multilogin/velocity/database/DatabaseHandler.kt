package moe.caa.multilogin.velocity.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import moe.caa.multilogin.velocity.main.MultiLoginVelocity
import moe.caa.multilogin.velocity.util.camelCaseToUnderscore
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.filter
import kotlin.collections.forEach
import kotlin.collections.get
import kotlin.collections.joinToString
import kotlin.collections.mutableMapOf
import kotlin.collections.set
import kotlin.collections.toList

class DatabaseHandler(
    private val plugin: MultiLoginVelocity
) {
    private lateinit var dataSource: HikariDataSource
    private lateinit var database: Database

    fun init() {
        val configurationNode = plugin.configHandler.configurationNode.node("database")

        dataSource = HikariDataSource(readHikariConfig(configurationNode.node("data_source")))
        database = Database.connect(dataSource)

        useDatabase {
            SchemaUtils.createMissingTablesAndColumns(UserDataTableV3)
            SchemaUtils.createMissingTablesAndColumns(ProfileTableV3)
            SchemaUtils.createMissingTablesAndColumns(CacheWhitelistTableV2)
        }
    }

    fun close() {
        if (::dataSource.isInitialized) {
            dataSource.close()
        }
    }

    fun <T> useDatabase(database: Database = this.database, statement: Transaction.() -> T): T = transaction(database) {
        statement.invoke(this)
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