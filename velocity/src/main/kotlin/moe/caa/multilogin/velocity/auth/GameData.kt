package moe.caa.multilogin.velocity.auth

import moe.caa.multilogin.api.profile.GameProfile
import moe.caa.multilogin.velocity.config.service.BaseService
import moe.caa.multilogin.velocity.main.MultiLoginVelocity
import moe.caa.multilogin.velocity.util.gameData

sealed class GameData(
    val inGameProfile: GameProfile
) {
    val lazyPlayer = lazy {
        MultiLoginVelocity.instance.proxyServer.allPlayers.firstOrNull {
            it.gameData == this
        }
    }
}

class OfflineGameData(inGameProfile: GameProfile) : GameData(inGameProfile) {
    var verified = false
}

class OnlineGameData(
    val userProfile: GameProfile,
    val service: BaseService,
    inGameProfile: GameProfile
) : GameData(inGameProfile)