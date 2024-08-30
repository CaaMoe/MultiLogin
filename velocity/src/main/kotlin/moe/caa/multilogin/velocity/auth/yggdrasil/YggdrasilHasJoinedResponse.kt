package moe.caa.multilogin.velocity.auth.yggdrasil

import kotlinx.serialization.Serializable
import moe.caa.multilogin.api.profile.GameProfile
import moe.caa.multilogin.api.profile.GameProfile.Property
import moe.caa.multilogin.velocity.util.ser.YggdrasilHasJoinedResponseSerializer
import java.util.*

@Serializable(with = YggdrasilHasJoinedResponseSerializer::class)
data class YggdrasilHasJoinedResponse(
    val uuid: UUID,
    val username: String?,
    val properties: List<Property>,
    val actions: List<String>
) {
    fun toGameProfile(loginProfile: LoginProfile) = GameProfile(
        uuid, username ?: loginProfile.username, properties
    )
}