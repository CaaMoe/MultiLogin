package moe.caa.multilogin.core.manager

import moe.caa.multilogin.api.data.MultiLoginPlayerData
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
    val preLoginData: MutableMap<UUID, VerifiedData> = ConcurrentHashMap()
    val verifiedProfileData: MutableMap<UUID, VerifiedData> = ConcurrentHashMap()

    fun init() {
        // 删掉 preLoginData
        MultiCore.instance.plugin.bootstrap.scheduler.runTaskLaterAsync({
            preLoginData.values.removeIf { System.currentTimeMillis() - it.signTimeMills > 1000 * 10 }
        }, 1000 * 10)

        // 删掉已经离线的玩家数据
        MultiCore.instance.plugin.bootstrap.scheduler.runTaskLaterAsync({
            val onlinePlayerUUIDs =
                MultiCore.instance.plugin.playerManager.getOnlinePlayers().map { it.inGameProfile.uuid }
            val shouldRemove = verifiedProfileData.keys.filter { !onlinePlayerUUIDs.contains(it) }
            MultiCore.instance.plugin.bootstrap.scheduler.runTaskLaterAsync({
                shouldRemove.filter { MultiCore.instance.plugin.playerManager.getOnlinePlayer(it) == null }.forEach {
                    verifiedProfileData.remove(it)
                }
            }, 1000 * 10)
        }, 1000 * 60)
    }

    fun handlePlayerLogin(playerInfo: IPlayerManager.IPlayerInfo): PlayerJoinHandleResult {
        val verifiedData = preLoginData.remove(playerInfo.inGameProfile.uuid)
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

            verifiedProfileData[playerInfo.inGameProfile.uuid] = verifiedData
        }

        return PlayerJoinHandleSuccessResult
    }

    fun handlePlayerJoin(playerInfo: IPlayerManager.IPlayerInfo){
        if (MessagePrompt.showWelcomeMessage) {
            val verifiedData = verifiedProfileData[playerInfo.inGameProfile.uuid]
            val component = if(verifiedData == null){
                language("welcome_message_unidentified")
                    .replace("%username%", playerInfo.inGameProfile.username)
                    .replace("%uuid%", playerInfo.inGameProfile.uuid.toString())
            } else {
                language("welcome_message")
                    .replace("%login_username%", verifiedData.loginProfile.username)
                    .replace("%login_uuid%", verifiedData.loginProfile.uuid.toString())
                    .replace("%service_name%", verifiedData.authService.serviceName)
                    .replace("%service_id%", verifiedData.authService.serviceId.toString())
                    .replace("%profile_username%", playerInfo.inGameProfile.username)
                    .replace("%profile_uuid%", playerInfo.inGameProfile.uuid.toString())
            }

            MultiCore.instance.plugin.bootstrap.scheduler.runTaskLaterAsync({
                playerInfo.audience.sendMessage(component)
            }, 1000)
        }
    }

    data class VerifiedData (
        private val loginProfile: GameProfile,
        private val baseService: BaseService,
        private val inGameProfile: GameProfile,
        val signTimeMills: Long
    ) : MultiLoginPlayerData {
        override fun getLoginProfile() = loginProfile

        override fun getAuthService() = baseService

        override fun getInGameProfile() = inGameProfile
    }

    sealed interface PlayerJoinHandleResult

    data object PlayerJoinHandleSuccessResult : PlayerJoinHandleResult
    class PlayerJoinHandleFailureResult(val kickResult: Component): PlayerJoinHandleResult
}