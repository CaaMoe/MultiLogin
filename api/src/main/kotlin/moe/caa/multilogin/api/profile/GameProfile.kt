package moe.caa.multilogin.api.profile

import java.util.*

/**
 * 表示一个游戏档案
 */
data class GameProfile(
    val uuid: UUID,
    val username: String,
    val properties: List<Property>,
) {
    fun withUUID(uuid: UUID) = GameProfile(uuid, username, properties)
    fun withName(username: String) = GameProfile(uuid, username, properties)

    data class Property(
        val name: String,
        val value: String,
        val signature: String?
    )
}