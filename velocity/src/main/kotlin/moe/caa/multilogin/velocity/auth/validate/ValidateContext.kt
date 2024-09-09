package moe.caa.multilogin.velocity.auth.validate

import moe.caa.multilogin.api.profile.GameProfile
import moe.caa.multilogin.velocity.auth.OnlineGameData
import moe.caa.multilogin.velocity.config.service.BaseService

data class ValidateContext(
    val baseService: BaseService,
    val userGameProfile: GameProfile
) {
    lateinit var profileGameProfile: GameProfile

    fun toGameData() = OnlineGameData(
        userGameProfile,
        baseService,
        profileGameProfile
    )
}
