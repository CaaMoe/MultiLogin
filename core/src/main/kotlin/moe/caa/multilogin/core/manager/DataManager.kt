package moe.caa.multilogin.core.manager

import moe.caa.multilogin.api.profile.GameProfile
import moe.caa.multilogin.core.main.MultiCore
import moe.caa.multilogin.core.plugin.IPlayerManager
import moe.caa.multilogin.core.resource.configuration.GeneralConfiguration
import moe.caa.multilogin.core.resource.configuration.MessagePrompt
import moe.caa.multilogin.core.resource.configuration.service.BaseService
import moe.caa.multilogin.core.resource.message.language
import moe.caa.multilogin.core.resource.message.replace
import moe.caa.multilogin.core.util.logWarn
import net.kyori.adventure.text.Component
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class DataManager {
    val verifiedProfileData: MutableMap<UUID, VerifiedData> = ConcurrentHashMap()

    fun handlePlayerLogin(playerInfo: IPlayerManager.IPlayerInfo): PlayerJoinHandleResult {
        val verifiedData = verifiedProfileData[playerInfo.inGameProfile.uuid]
        if(verifiedData == null){
            if (GeneralConfiguration.forceUseLogin){
                return PlayerJoinHandleFailureResult(language("login_failed_force_use_login"))
            }
            logWarn("The player with in game UUID ${playerInfo
                .inGameProfile.uuid} and name ${playerInfo
                    .inGameProfile.username} is not logged into the server by MultiLogin, some features will be disabled for him.")
        } else {
            val time = System.currentTimeMillis() - verifiedData.signTimeMills
            if(time > 5 * 1000){
                logWarn("Players with in game UUID ${playerInfo
                    .inGameProfile.uuid} and name ${playerInfo
                        .inGameProfile.username} are taking too long to log in after verification, reached $time milliseconds. Is it the same person?")
            }
        }

        return PlayerJoinHandleSuccessResult
    }

    fun handlePlayerJoin(playerInfo: IPlayerManager.IPlayerInfo){
        if (MessagePrompt.showWelcomeMessage) {
            val verifiedData = verifiedProfileData[playerInfo.inGameProfile.uuid]
            val component = if(verifiedData == null){
                language("message_welcome_join_unidentified")
                    .replace("%profile_username%", playerInfo.inGameProfile.username)
                    .replace("%profile_uuid%", playerInfo.inGameProfile.uuid.toString())
            } else {
                language("message_welcome_join")
                    .replace("%login_username%", verifiedData.loginProfile.username)
                    .replace("%login_uuid%", verifiedData.loginProfile.uuid.toString())
                    .replace("%service_name%", verifiedData.baseService.serviceName)
                    .replace("%service_id%", verifiedData.baseService.serviceId.toString())
                    .replace("%profile_username%", playerInfo.inGameProfile.username)
                    .replace("%profile_uuid%", playerInfo.inGameProfile.uuid.toString())
            }

            MultiCore.instance.plugin.bootstrap.scheduler.runTaskLaterAsync({
                playerInfo.audience.sendMessage(component)
            },20)
        }
    }

    data class VerifiedData (
        val loginProfile: GameProfile,
        val baseService: BaseService,
        val inGameProfile: GameProfile,
        val signTimeMills: Long
    )

    sealed interface PlayerJoinHandleResult

    data object PlayerJoinHandleSuccessResult : PlayerJoinHandleResult
    class PlayerJoinHandleFailureResult(val kickResult: Component): PlayerJoinHandleResult
}