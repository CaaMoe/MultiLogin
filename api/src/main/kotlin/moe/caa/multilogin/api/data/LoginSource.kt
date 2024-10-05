package moe.caa.multilogin.api.data

import moe.caa.multilogin.api.profile.MinimalProfile
import moe.caa.multilogin.api.service.BaseService

/**
 * 表示一个玩家登录来源
 */
data class LoginSource(
    val service: BaseService,
    val userProfile: MinimalProfile,
    val inGameProfile: MinimalProfile
)