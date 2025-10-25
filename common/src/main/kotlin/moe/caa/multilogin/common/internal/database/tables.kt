package moe.caa.multilogin.common.internal.database

import org.jetbrains.exposed.v1.core.dao.id.CompositeIdTable
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable

sealed interface MultiLoginTable

object UserTable : MultiLoginTable, IntIdTable("multilogin_user_data", "user_id") {
    val uuid = uuid("user_uuid")
    val loginMethod = varchar("login_method", 64)
    val lastKnownName = varchar("last_known_name", 256).index()

    init {
        uniqueIndex(uuid, loginMethod)
    }
}

object CurrentSelectSlot : MultiLoginTable, CompositeIdTable("multilogin_current_select_slot") {
    val userID = reference("user_id", UserTable.id)
    val selectedProfileSlot = reference("selected_profile_slot", ProfileTable.profileSlot)

    override val primaryKey = PrimaryKey(userID)
}

object ProfileTable : MultiLoginTable, IntIdTable("multilogin_profiles", "profile_id") {
    val ownerUserID = reference("owner_user_id", UserTable.id)
    val profileSlot = integer("profile_slot")

    val profileUUID = uuid("profile_uuid").index()
    val profileLowerCastName = varchar("profile_lower_cast_name", 256).uniqueIndex()
    val profileOriginalName = varchar("profile_original_name", 256).index()

    init {
        uniqueIndex(ownerUserID, profileSlot)
    }
}