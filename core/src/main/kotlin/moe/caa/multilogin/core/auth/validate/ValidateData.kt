package moe.caa.multilogin.core.auth.validate

import moe.caa.multilogin.api.profile.GameProfile
import moe.caa.multilogin.core.database.v4.UnmodifiedProfileData
import moe.caa.multilogin.core.database.v4.UnmodifiedUserData
import moe.caa.multilogin.core.resource.configuration.service.BaseService

data class ValidateData(
    val service: BaseService,
    val loginProfile: GameProfile
) {
    var unmodifiedUserData: UnmodifiedUserData? = null
    var unmodifiedProfileData: UnmodifiedProfileData? = null

    var nameAutoRepeatCorrected: Boolean = false
}