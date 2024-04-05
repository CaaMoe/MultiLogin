package moe.caa.multilogin.core.sql

import org.jetbrains.exposed.dao.id.IntIdTable

object InGameProfileV3Table : IntIdTable(name = "multilogin_in_game_profile_v3") {
    val user = reference("user_id", UserDataV3Table)
    val inGameUUID = uuid("in_game_uuid").uniqueIndex()
    val currentUsernameLowerCase = varchar("current_username_lower_case", 64).uniqueIndex()
    val currentUsernameOriginal = varchar("current_username_original", 64).uniqueIndex()
}

object UserDataV3Table : IntIdTable(name = "multilogin_user_data_v3") {
    val profile = reference("profile_id", InGameProfileV3Table).nullable()

    val onlineUUID = uuid("online_uuid")
    val serviceId = integer("service_id")
    val onlineName = varchar("online_name", 64).nullable()

    /**
     * 0: No whitelist
     * 1: Whitelist
     * 2: Caching whitelist
     */
    val whitelist = integer("whitelist").check("check_whitelist_range") { it.between(0, 2) }

    init {
        uniqueIndex(serviceId, onlineUUID)
    }
}