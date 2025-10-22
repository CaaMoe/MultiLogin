package moe.caa.multilogin.common.internal.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import moe.caa.multilogin.common.internal.main.MultiCore
import moe.caa.multilogin.common.internal.util.IOUtil
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.migration.jdbc.MigrationUtils
import java.io.FileReader
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.*

class DatabaseHandler(
    private val core: MultiCore
) {
    private lateinit var dataSource: HikariDataSource
    private lateinit var database: Database

    fun initDatabase() {
        val hikariConfigurationPath =
            core.platform.platformConfigPath.resolve(core.mainConfig.databaseConfiguration.get())

        if (!Files.exists(hikariConfigurationPath)) {
            Objects.requireNonNull<ByteArray>(IOUtil.readNestResource("default_hikari.properties"))
                .apply {
                    var input = String(this, StandardCharsets.UTF_8)
                    input = input.replace(
                        "{{data_directory}}",
                        core.platform.platformConfigPath.toFile().absolutePath.replace("\\", "/")
                    )
                    Files.writeString(hikariConfigurationPath, input, StandardCharsets.UTF_8)
                }
        }

        val properties = Properties()
        FileReader(hikariConfigurationPath.toFile()).use { reader ->
            properties.load(reader)
        }
        val config = HikariConfig(properties)
        dataSource = HikariDataSource(config)
        database = Database.connect(dataSource)
        transaction(database) {
            val tables = MultiLoginTable::class.sealedSubclasses.map { it.objectInstance!! as Table }.toTypedArray()
            SchemaUtils.create(*tables)
            MigrationUtils.statementsRequiredForDatabaseMigration(*tables)
        }
    }

    fun close() {
        if (this::dataSource.isInitialized) {
            dataSource.close()
        }
    }
}
