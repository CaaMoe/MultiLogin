package moe.caa.multilogin.velocity.manager

import moe.caa.multilogin.api.exception.InGameProfileNotFoundException
import moe.caa.multilogin.api.exception.ProfileConflictException
import moe.caa.multilogin.api.manager.ProfileManager
import moe.caa.multilogin.api.profile.MinimalProfile
import moe.caa.multilogin.velocity.database.ProfileTableV3
import moe.caa.multilogin.velocity.main.MultiLoginVelocity
import moe.caa.multilogin.velocity.util.causeIsSQLIntegrityConstraintViolationException
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.update
import java.util.*

class VelocityProfileManager(
    private val plugin: MultiLoginVelocity
): ProfileManager {
    override fun findInGameProfile(username: String): MinimalProfile? {
        return plugin.databaseHandler.useDatabase {
            ProfileTableV3.select(
                ProfileTableV3.id,
                ProfileTableV3.currentUserNameOriginal
            ).where {
                ProfileTableV3.currentUserNameLowerCase.lowerCase() eq username.lowercase()
            }.limit(1).map {
                MinimalProfile(
                    it[ProfileTableV3.id].value,
                    it[ProfileTableV3.currentUserNameOriginal],
                )
            }.firstOrNull()
        }
    }

    override fun findInGameProfile(profileId: UUID): MinimalProfile? {
        return plugin.databaseHandler.useDatabase {
            ProfileTableV3.select(
                ProfileTableV3.id,
                ProfileTableV3.currentUserNameOriginal
            ).where {
                ProfileTableV3.id eq profileId
            }.limit(1).map {
                MinimalProfile(
                    it[ProfileTableV3.id].value,
                    it[ProfileTableV3.currentUserNameOriginal],
                )
            }.firstOrNull()
        }
    }

    override fun createInGameProfile(id: UUID, username: String): MinimalProfile {
        try {
            return plugin.databaseHandler.useDatabase {
                if(!ProfileTableV3.select(
                        ProfileTableV3.id
                    ).where{
                        ProfileTableV3.currentUserNameLowerCase.lowerCase() eq username.lowercase()
                    }.empty()){
                    throw ProfileConflictException("username: $username")
                }

                if(!ProfileTableV3.select(
                        ProfileTableV3.id
                    ).where{
                        ProfileTableV3.id eq id
                    }.empty()){
                    throw ProfileConflictException("id: $id")
                }

                ProfileTableV3.insert {
                    it[ProfileTableV3.id] = id
                    it[currentUserNameOriginal] = username
                    it[currentUserNameLowerCase] = username.lowercase()
                }
                return@useDatabase MinimalProfile(id, username)
            }
        } catch (t: Throwable){
            if (t.causeIsSQLIntegrityConstraintViolationException()) {
                throw ProfileConflictException("id: $id, username: $username", t)
            }
            throw t
        }
    }

    override fun renameInGameProfile(id: UUID, newUsername: String) {
        if(ProfileTableV3.select(
                ProfileTableV3.id
            ).where{
                ProfileTableV3.id eq id
            }.empty()){
            throw InGameProfileNotFoundException("id: $id")
        }

        kotlin.runCatching {
            ProfileTableV3.update({
                ProfileTableV3.id eq id
            }) {
                it[currentUserNameOriginal] = newUsername
                it[currentUserNameLowerCase] = newUsername.lowercase()
            }
        }.onFailure {
            if (it.causeIsSQLIntegrityConstraintViolationException()) {
                throw ProfileConflictException("newUsername: $newUsername", it)
            }
            throw it
        }
    }
}