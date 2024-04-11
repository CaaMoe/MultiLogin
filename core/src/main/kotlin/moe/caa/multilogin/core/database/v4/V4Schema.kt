package moe.caa.multilogin.core.database.v4

import org.jetbrains.exposed.dao.id.IntIdTable

object InGameProfileV4 : IntIdTable(name = "multilogin_in_game_profile_v4") {

    /**
     * Null for any service. (for caching whitelist).
     */
    val serviceId = integer("service_id").nullable()

    /**
     * If user is in caching whitelist, generate a random uuid first.
     */
    val loginUuid = uuid("login_uuid").nullable()

    /**
     * The uuid actually used in game.
     * Same as login uuid in most case.
     * But different when login uuid is already exists in game.
     */
    val profileUuid = uuid("profile_uuid").uniqueIndex().nullable()
    val username = varchar("username", 64).uniqueIndex()
    val usernameLowerCase = varchar("username_lower_case", 64).uniqueIndex()

    /**
     * 0: No whitelist
     * 1: Whitelist
     * 2: Caching whitelist
     */
    val whitelist = integer("whitelist").check("check_whitelist_range") { it.between(0, 2) }

    val redirectTo = reference("redirect_to_profile_id", InGameProfileV4).nullable()

    init {
        uniqueIndex(serviceId, loginUuid)
    }
}
