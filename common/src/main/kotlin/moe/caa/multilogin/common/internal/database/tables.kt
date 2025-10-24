package moe.caa.multilogin.common.internal.database

import org.jetbrains.exposed.v1.core.dao.id.CompositeIdTable
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable

sealed interface MultiLoginTable

object UserTable : MultiLoginTable, IntIdTable("multilogin_user_data", "user_id") {
    val uuid = uuid("user_uuid")
    val loginMethod = varchar("login_method", 64)
    val lastKnownName = varchar("last_known_name", 256)
    val selectProfile = reference("select_profile", ProfileTable.id).nullable()

    init {
        uniqueIndex(uuid, loginMethod)
        index(false, lastKnownName)
        index(false, selectProfile)
    }
}

object UserHaveProfilesTable : MultiLoginTable, CompositeIdTable("multilogin_user_have_profiles") {
    val user = reference("user_id", UserTable.id)
    val profile = reference("profile_id", ProfileTable.id)

    init {
        uniqueIndex(user, profile)
    }
}

object ProfileTable : MultiLoginTable, IntIdTable("multilogin_profiles", "profile_id") {
    val profileUUID = uuid("profile_uuid")
    val profileLowerCastName = varchar("profile_lower_cast_name", 256)
    val profileOriginalName = varchar("profile_original_name", 256)

    init {
        uniqueIndex(profileUUID)
        uniqueIndex(profileLowerCastName)
        index(true, profileOriginalName)
    }
}