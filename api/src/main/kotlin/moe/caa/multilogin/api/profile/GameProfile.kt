package moe.caa.multilogin.api.profile

import java.util.*

data class GameProfile(
    val uuid: UUID,
    val name: String,
    val properties: List<Pair<String, Property>>
) {
    fun getProperties(name: String) = properties.filter { it.first == name }

    data class Property(
        val name: String,
        val value: String,
        val signature: String?
    )
}
