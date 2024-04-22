package moe.caa.multilogin.core.auth.validate

import moe.caa.multilogin.api.profile.GameProfile
import moe.caa.multilogin.core.resource.configuration.service.BaseService

data class ValidateProfile(
    val service: BaseService,
    val gameProfile: GameProfile
)