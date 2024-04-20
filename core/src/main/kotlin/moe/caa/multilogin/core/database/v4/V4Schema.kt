package moe.caa.multilogin.core.database.v4

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object Profiles : IntIdTable(name = "multilogin_in_game_profile_v4") {

    /**
     * Null for any service. (for caching whitelist).
     */
    val serviceId = integer("service_id").nullable()

    /**
     * The uuid in client hello packet.
     * If user is in caching whitelist and not provided, generate a random uuid first.
     */
    val loginUuid = uuid("login_uuid")

    /**
     * The uuid actually used in game.
     * Same as login uuid in most case.
     * But different when login uuid is already exists in game.
     */
    val profileUuid = uuid("profile_uuid").nullable()

    /**
     * The username in client hello packet.
     */
    val loginUsername = varchar("login_username", 255)  // Use 255 instead 64, 万一有愚蠢的 yggdrasil 实现呢（（（

    /**
     * The username actually used in game.
     * MultiLogin will rename when username was conflict.
     */
    val username = varchar("username", 255).nullable()
    val usernameLowerCase = varchar("username_lower_case", 255).nullable()

    /**
     * 0: No whitelist
     * 1: Whitelist
     * 2: Caching whitelist
     */
    val whitelist = integer("whitelist").check("check_whitelist_range") { it.between(0, 2) }

    val redirectTo = integer("redirect_to_profile_id")
        .references(Profiles.id, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.RESTRICT)
        .nullable()
        .default(null)

    init {
        uniqueIndex(serviceId, loginUuid)
        uniqueIndex(serviceId, loginUsername)
    }
}

class Profile(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Profile>(Profiles)

    var redirectTo by Profile referencedOn Profiles.id
}
