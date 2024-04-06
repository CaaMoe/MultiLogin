package moe.caa.multilogin.core.sql

import org.jetbrains.exposed.dao.id.IntIdTable

object InGameProfileV3Table : IntIdTable(name = "multilogin_in_game_profile_v3") {

    /**
     * null for any service. (for caching whitelist).
     */
    val serviceId = integer("service_id").nullable()

    val inGameUUID = uuid("profile_uuid").uniqueIndex()
    val currentUsernameLowerCase = varchar("current_username_lower_case", 64).uniqueIndex()
    val currentUsernameOriginal = varchar("current_username_original", 64).uniqueIndex()

    /**
     * 0: No whitelist
     * 1: Whitelist
     * 2: Caching whitelist
     */
    val whitelist = integer("whitelist").check("check_whitelist_range") { it.between(0, 2) }

    val redirectTo = reference("redirect_to_profile_id", InGameProfileV3Table).nullable()
}
