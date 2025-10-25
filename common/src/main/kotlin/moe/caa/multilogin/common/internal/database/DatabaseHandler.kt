package moe.caa.multilogin.common.internal.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import moe.caa.multilogin.common.internal.config.authentication.AuthenticationConfig
import moe.caa.multilogin.common.internal.data.GameProfile
import moe.caa.multilogin.common.internal.data.Profile
import moe.caa.multilogin.common.internal.data.User
import moe.caa.multilogin.common.internal.main.MultiCore
import moe.caa.multilogin.common.internal.util.IOUtil
import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.*
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
            core.platform.platformConfigPath.resolve(core.mainConfig.databaseConfigPath.get())

        if (!Files.exists(hikariConfigurationPath)) {
            Objects.requireNonNull(IOUtil.readNestResource("default_hikari.properties"))
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

    private fun <T> useTransaction(statement: JdbcTransaction.() -> T): T = transaction(database, statement)

    fun updateOrCreateUser(authenticationConfig: AuthenticationConfig, authenticatedProfile: GameProfile) =
        useTransaction {
            getUsers0 {
                (UserTable.loginMethod eq authenticationConfig.id.get()) and
                        (UserTable.uuid eq authenticatedProfile.uuid)
            }.firstOrNull()?.apply {
                updateUserLastKnownName(this.userID, authenticatedProfile.username)
                return@useTransaction copy(username = authenticatedProfile.username())
            }

            return@useTransaction getUsers0 {
                UserTable.id eq UserTable.insertAndGetId {
                    it[UserTable.uuid] = authenticatedProfile.uuid()
                    it[UserTable.loginMethod] = authenticationConfig.id.get()
                    it[UserTable.lastKnownName] = authenticatedProfile.username
                }
            }.first()
        }

    fun getUserCurrentSelectProfileSlot(userID: Int) = useTransaction {
        CurrentSelectSlot.selectAll().where {
            CurrentSelectSlot.userID eq userID
        }.map {
            it[CurrentSelectSlot.selectedProfileSlot]
        }.firstOrNull()
    }

    fun updateUserCurrentSelectProfileSlot(userID: Int, selectedSlot: Int) = useTransaction {
        runCatching {
            if (getUserCurrentSelectProfileSlot(userID) == null) {
                CurrentSelectSlot.insert {
                    it[CurrentSelectSlot.selectedProfileSlot] = selectedSlot
                    it[CurrentSelectSlot.userID] = userID
                }
            }
        }
        CurrentSelectSlot.update({ CurrentSelectSlot.userID eq userID }) {
            it[CurrentSelectSlot.selectedProfileSlot] = selectedSlot
        }
    }

    fun removeCurrentSelectProfile(userID: Int) = useTransaction {
        CurrentSelectSlot.deleteWhere {
            CurrentSelectSlot.userID eq userID
        }
    }

    fun getProfilesByOwnerID(ownerUserID: Int) = getProfiles0 {
        ProfileTable.ownerUserID eq ownerUserID
    }.associateBy { it.profileSlot }

    fun getProfileByProfileName(profileName: String) = getProfiles0 {
        ProfileTable.profileLowerCastName.lowerCase() eq profileName.lowercase()
    }.firstOrNull()


    fun getProfileByProfileUUID(profileUUID: UUID) = getProfiles0 {
        ProfileTable.profileUUID eq profileUUID
    }.firstOrNull()

    fun getProfileByOwnerIDAndSlotID(ownerUserID: Int, slotID: Int) = getProfiles0 {
        (ProfileTable.ownerUserID eq ownerUserID) and (ProfileTable.profileSlot eq slotID)
    }.firstOrNull()

    private fun updateUserLastKnownName(userID: Int, lastKnownName: String) = useTransaction {
        UserTable.update({ UserTable.id eq userID }) {
            it[UserTable.lastKnownName] = lastKnownName
        }
    }

    private fun getUsers0(predicate: () -> Op<Boolean>) = useTransaction {
        UserTable.selectAll().where(predicate).limit(1).map {
            User(
                it[UserTable.id].value,
                it[UserTable.loginMethod],
                it[UserTable.uuid],
                it[UserTable.lastKnownName]
            )
        }
    }

    private fun getProfiles0(predicate: () -> Op<Boolean>) = useTransaction {
        ProfileTable.selectAll().where(predicate).map {
            Profile(
                it[ProfileTable.id].value,
                it[ProfileTable.ownerUserID],
                it[ProfileTable.profileSlot],
                it[ProfileTable.profileUUID],
                it[ProfileTable.profileOriginalName],
            )
        }
    }

    fun createProfile(
        profileUUID: UUID,
        profileName: String,
        ownedUserID: Int,
        putProfileSlot: Int
    ) = useTransaction {
        getProfiles0 {
            ProfileTable.id eq ProfileTable.insertAndGetId {
                it[ProfileTable.profileUUID] = profileUUID
                it[ProfileTable.profileLowerCastName] = profileName.lowercase()
                it[ProfileTable.profileOriginalName] = profileName
                it[ProfileTable.ownerUserID] = ownedUserID
                it[ProfileTable.profileSlot] = putProfileSlot
            }
        }.first()
    }

    fun getUserByUserID(userID: Int) = getUsers0 {
        UserTable.id eq userID
    }.firstOrNull()

    fun getProfileByProfileID(profileID: Int) = getProfiles0 {
        ProfileTable.id eq profileID
    }.firstOrNull()
}
