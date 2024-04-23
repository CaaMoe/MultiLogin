package moe.caa.multilogin.core.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import moe.caa.multilogin.core.main.MultiCore
import moe.caa.multilogin.core.util.camelCaseToUnderscore
import moe.caa.multilogin.core.util.logInfo
import org.jetbrains.exposed.sql.Database
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Modifier

class SQLHandler {
    private lateinit var database: Database
    private lateinit var dataSource: HikariDataSource
    lateinit var tableHandler: TableHandler

    fun init() {
        val configurationNode = MultiCore.instance.configurationHandler.configurationNode!!.node("database")
        val sqlDriverType = configurationNode.node("sql_driver_type")
            .get(SQLDriverType::class.java, SQLDriverType.MYSQL)
        val shouldLoading = sqlDriverType.name.lowercase()

        logInfo("Loading the jdbc driver of ${shouldLoading}...")
        MultiCore.instance.plugin.bootstrap.pluginLoader.loadLibraries("sql_driver_$shouldLoading")

        dataSource = HikariDataSource(readHikariConfig(configurationNode))
        database = Database.connect(dataSource)
        tableHandler = TableHandler(database)

        tableHandler.init()
    }

    fun close() {
        if (::dataSource.isInitialized) {
            dataSource.close()
        }
    }

    private fun readHikariConfig(configurationNode: ConfigurationNode) = HikariConfig().apply {
        javaClass.methods
            .filter { !Modifier.isStatic(it.modifiers) }
            .filter { it.name.startsWith("set") }
            .filter { it.parameters.size == 1 }
            .forEach {
                it.isAccessible = true
                val key = it.name.substring(3).camelCaseToUnderscore()

                if (configurationNode.hasChild(key)) {
                    val subNode = configurationNode.node(key)
                    when (it.parameters[0].type) {
                        Boolean::class.java -> {
                            it.invoke(this, subNode.boolean)
                        }

                        String::class.java -> {
                            it.invoke(this, subNode.string)
                        }

                        Long::class.java -> {
                            it.invoke(this, subNode.long)
                        }

                        Int::class.java -> {
                            it.invoke(this, subNode.int)
                        }
                    }
                }
            }
    }

    enum class SQLDriverType {
        POSTGRES,
        POSTGRES_NG,
        MYSQL,
        MARIADB,
        ORACLE,
        SQLITE,
        H2,
        SQLSERVER,
    }
}

