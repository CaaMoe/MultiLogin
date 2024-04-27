package moe.caa.multilogin.core.auth.validate

import moe.caa.multilogin.api.profile.GameProfile
import moe.caa.multilogin.core.database.v4.ProfileData
import moe.caa.multilogin.core.database.v4.UserData
import moe.caa.multilogin.core.resource.configuration.service.BaseService

data class ValidateData(
    val service: BaseService,
    val loginProfile: GameProfile
) {
    lateinit var userData: UserData
    lateinit var profileData: ProfileData

    var nameAutoRepeatCorrected: Boolean = false
}