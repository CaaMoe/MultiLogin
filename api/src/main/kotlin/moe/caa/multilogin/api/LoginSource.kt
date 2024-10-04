package moe.caa.multilogin.api

import moe.caa.multilogin.api.profile.GameProfile
import moe.caa.multilogin.api.service.BaseService

data class LoginSource(
    val service: BaseService,
    val userProfile: GameProfile,
    val inGameProfile: GameProfile
)