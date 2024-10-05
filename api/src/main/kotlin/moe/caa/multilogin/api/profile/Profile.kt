package moe.caa.multilogin.api.profile

import java.util.*

/**
 * 完整的游戏档案
 */
data class GameProfile(
    val minimalProfile: MinimalProfile,
    val properties: List<Property>
)

/**
 * 只包含id和username的游戏档案
 */
data class MinimalProfile(
    val id: UUID,
    val username: String
)

/**
 * 游戏档案数据
 */
data class Property(
    val name: String,
    val value: String,
    val signature: String?
)