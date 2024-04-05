package moe.caa.multilogin.core.sql


import org.jetbrains.exposed.sql.Table


class InGameProfileV3Table(tName: String) : Table(tName) {

    val inGameUUID = binary("in_game_uuid", 16)
    val currentUsernameLowerCase = varchar("current_username_lower_case", 64).uniqueIndex().nullable()
    val currentUsernameOriginal = varchar("current_username_original", 64).nullable()

    override val primaryKey = PrimaryKey(inGameUUID)
}

class UserDataV3Table(tName: String) : Table(tName) {
    val onlineUUID = binary("online_uuid", 16)
    val serviceId = integer("service_id")
    val onlineName = varchar("online_name", 64).nullable()
    val inGameProfileUUID = binary("in_game_profile_uuid", 16).nullable()
    val whitelist = bool("whitelist")

    override val primaryKey = PrimaryKey(onlineUUID, serviceId)
}