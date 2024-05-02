package `fun`.iiii.multilogin.velocity.core.listener

import com.velocitypowered.api.event.ResultedEvent
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.connection.LoginEvent
import `fun`.iiii.multilogin.velocity.core.main.MultiLoginVelocityCore
import `fun`.iiii.multilogin.velocity.core.manager.VelocityPlayerManager
import moe.caa.multilogin.core.manager.DataManager

class Listener(private val core: MultiLoginVelocityCore) {

    @Subscribe
    fun onJoin(loginEvent: LoginEvent) {
        val playerInfo = VelocityPlayerManager.VelocityPlayerInfo(loginEvent.player)
        val handleResult = core.multiCore.dataManager.handlePlayerLogin(playerInfo)
        if(handleResult is DataManager.PlayerJoinHandleFailureResult){
            loginEvent.result = ResultedEvent.ComponentResult.denied(handleResult.kickResult)
            return
        }

        core.multiCore.dataManager.handlePlayerJoin(playerInfo)
    }
}