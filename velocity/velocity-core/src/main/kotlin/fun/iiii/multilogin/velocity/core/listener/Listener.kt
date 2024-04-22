package `fun`.iiii.multilogin.velocity.core.listener

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.connection.PostLoginEvent
import `fun`.iiii.multilogin.velocity.core.main.MultiLoginVelocityCore
import `fun`.iiii.multilogin.velocity.core.manager.VelocityPlayerManager

class Listener(private val core: MultiLoginVelocityCore) {

    @Subscribe
    fun onJoin(postLoginEvent: PostLoginEvent) {
        core.multiCore.playerDataManager.handlePlayerJoin(VelocityPlayerManager.VelocityPlayerInfo(postLoginEvent.player))
    }

    @Subscribe
    fun onDisconnect(disconnectEvent: DisconnectEvent) {
        core.multiCore.playerDataManager.handlePlayerQuit(VelocityPlayerManager.VelocityPlayerInfo(disconnectEvent.player))
    }
}