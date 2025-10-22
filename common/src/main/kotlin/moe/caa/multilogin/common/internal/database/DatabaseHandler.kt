package moe.caa.multilogin.common.internal.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import moe.caa.multilogin.common.internal.main.MultiCore
import moe.caa.multilogin.common.internal.profile.ProfileManager
import moe.caa.multilogin.common.internal.util.IOUtil
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.lowerCase
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.select
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

    fun createProfile(profileUUID: UUID, profileName: String) = transaction {
        getProfileByID(ProfileTable.insertAndGetId {
            it[ProfileTable.profileUUID] = profileUUID
            it[ProfileTable.profileLowerCastName] = profileName.lowercase()
            it[ProfileTable.profileOriginalName] = profileName
        }.value)
    }

    private fun getProfile0(predicate: () -> Op<Boolean>) = transaction(database) {
        ProfileTable.select(
            ProfileTable.id,
            ProfileTable.profileUUID,
            ProfileTable.profileOriginalName
        ).where(predicate).limit(1).map {
            ProfileManager.Profile(
                it[ProfileTable.id].value,
                it[ProfileTable.profileUUID],
                it[ProfileTable.profileOriginalName]
            )
        }.firstOrNull()
    }

    fun getProfileByID(profileID: Int) = getProfile0 {
        ProfileTable.id eq profileID
    }

    fun getProfileByName(profileName: String) = getProfile0 {
        ProfileTable.profileLowerCastName.lowerCase() eq profileName.lowercase()
    }


    fun getProfileByUUID(profileUUID: UUID) = getProfile0 {
        ProfileTable.profileUUID eq profileUUID
    }
}
