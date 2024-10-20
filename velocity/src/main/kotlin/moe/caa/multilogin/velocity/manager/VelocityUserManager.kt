package moe.caa.multilogin.velocity.manager

import moe.caa.multilogin.api.exception.InGameProfileNotFoundException
import moe.caa.multilogin.api.manager.UserManager
import moe.caa.multilogin.api.profile.MinimalProfile
import moe.caa.multilogin.api.service.BaseService
import moe.caa.multilogin.velocity.database.UserDataTableV3
import moe.caa.multilogin.velocity.main.MultiLoginVelocity
import moe.caa.multilogin.velocity.util.requireRegistered
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.upsert
import java.util.*

class VelocityUserManager(
    private val plugin: MultiLoginVelocity
): UserManager {
    override fun findInGameProfile(service: BaseService, userProfile: MinimalProfile): MinimalProfile? {
        service.requireRegistered()
        return plugin.databaseHandler.useDatabase {
            UserDataTableV3.select(
                UserDataTableV3.inGameProfileUUID
            ).where {
                (UserDataTableV3.onlineUUID eq userProfile.id) and (UserDataTableV3.serviceId eq service.serviceId)
            }.limit(1).firstOrNull()?.let{ it[UserDataTableV3.inGameProfileUUID] }?.let {
                plugin.profileManager.findInGameProfile(it)
            }
        }
    }

    override fun findOrCreateInGameProfile(service: BaseService, userProfile: MinimalProfile): MinimalProfile {
        service.requireRegistered()
        TODO("Not yet implemented")
    }

    override fun setInGameProfile(service: BaseService, userProfile: MinimalProfile, inGameProfileUUID: UUID) {
        service.requireRegistered()
        plugin.profileManager.findInGameProfile(inGameProfileUUID)?: throw InGameProfileNotFoundException("id: $inGameProfileUUID")
        plugin.databaseHandler.useDatabase {
            UserDataTableV3.upsert(
                UserDataTableV3.onlineUUID, UserDataTableV3.serviceId
            ) {
                TODO("Not yet implemented")
            }
        }
    }
}