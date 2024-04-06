package moe.caa.multilogin.core.database.v4

import moe.caa.multilogin.core.config.MultiloginConfig
import org.jetbrains.exposed.dao.id.IntIdTable

object InGameProfileV4 : IntIdTable(name = "${MultiloginConfig.getInstance().tablePrefix}_in_game_profile_v4") {

    /**
     * null for any service. (for caching whitelist).
     */
    val serviceId = integer("service_id").nullable()

    /**
     * if user is in caching whitelist, generate a random uuid first.
     */
    val userUuid = uuid("user_uuid").nullable()

    val profileUuid = uuid("profile_uuid").uniqueIndex()
    val currentUsernameLowerCase = varchar("current_username_lower_case", 64).uniqueIndex()
    val currentUsernameOriginal = varchar("current_username_original", 64).uniqueIndex()

    /**
     * 0: No whitelist
     * 1: Whitelist
     * 2: Caching whitelist
     */
    val whitelist = integer("whitelist").check("check_whitelist_range") { it.between(0, 2) }

    val redirectTo = reference("redirect_to_profile_id", InGameProfileV4).nullable()

    init {
        uniqueIndex(serviceId, userUuid)
    }
}
