package moe.caa.multilogin.velocity.auth.validate

import moe.caa.multilogin.api.profile.GameProfile
import moe.caa.multilogin.velocity.config.service.BaseService

data class ValidateContext(
    val baseService: BaseService,
    val serviceGameProfile: GameProfile,
    var resultGameProfile: GameProfile = serviceGameProfile,
)
