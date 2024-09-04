package moe.caa.multilogin.velocity.auth.validate

import com.velocitypowered.proxy.connection.MinecraftConnection
import moe.caa.multilogin.api.profile.GameProfile
import moe.caa.multilogin.velocity.config.service.BaseService
import moe.caa.multilogin.velocity.main.InGameData

data class ValidateContext(
    val baseService: BaseService,
    val userGameProfile: GameProfile
) {
    lateinit var profileGameProfile: GameProfile

    fun toReadyLoginData(connection: MinecraftConnection) = InGameData.ReadyLoginEntry(
        connection,
        userGameProfile,
        baseService,
        profileGameProfile
    )
}
