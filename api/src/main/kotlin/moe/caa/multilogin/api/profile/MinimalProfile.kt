package moe.caa.multilogin.api.profile

import com.velocitypowered.api.util.GameProfile
import java.util.*


/**
 * 表示一个不包含档案属性的游戏档案信息
 */
data class MinimalProfile(
    val id: UUID,
    val username: String
) {
    fun toGameProfile(properties: List<GameProfile.Property> = emptyList()) = GameProfile(id, username, properties)

    companion object {
        fun fromGameProfile(gameProfile: GameProfile) = MinimalProfile(gameProfile.id, gameProfile.name)
    }
}