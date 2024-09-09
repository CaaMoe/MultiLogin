package moe.caa.multilogin.velocity.offline

import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.ResultedEvent
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.LoginEvent
import com.velocitypowered.api.event.player.GameProfileRequestEvent
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.proxy.connection.client.InitialInboundConnection
import com.velocitypowered.proxy.connection.client.LoginInboundConnection
import moe.caa.multilogin.api.profile.GameProfile
import moe.caa.multilogin.velocity.auth.OfflineGameData
import moe.caa.multilogin.velocity.database.ProfileTableV3
import moe.caa.multilogin.velocity.listener.PlayerLoginListener
import moe.caa.multilogin.velocity.main.MultiLoginVelocity
import moe.caa.multilogin.velocity.message.replace
import moe.caa.multilogin.velocity.util.gameData
import moe.caa.multilogin.velocity.util.getMultiLoginInboundHandler
import moe.caa.multilogin.velocity.util.toVelocityGameProfile
import org.jetbrains.exposed.sql.lowerCase
import kotlin.jvm.optionals.getOrNull

object OfflineLoginHandler {

    fun init() {
        MultiLoginVelocity.instance.proxyServer.eventManager.register(MultiLoginVelocity.instance, this)
    }

    @Subscribe(order = PostOrder.FIRST)
    fun onGameProfileRequest(event: GameProfileRequestEvent){
        val inboundHandler = (PlayerLoginListener.LOGIN_INBOUND_CONNECTION__DELEGATE_GETTER
            .invoke(event.connection as LoginInboundConnection)
                as InitialInboundConnection).connection.channel.getMultiLoginInboundHandler()

        when (inboundHandler.gameData) {
            is OfflineGameData, null -> {
                val useDatabase = MultiLoginVelocity.instance.database.useDatabase {
                    ProfileTableV3.select(
                        ProfileTableV3.id,
                        ProfileTableV3.currentUserNameOriginal
                    ).where {
                        ProfileTableV3.currentUserNameOriginal.lowerCase() eq event.originalProfile.name.lowercase()
                    }.limit(1).map {
                        GameProfile(
                            it[ProfileTableV3.id].value,
                            it[ProfileTableV3.currentUserNameOriginal],
                            emptyList()
                        )
                    }.firstOrNull()
                }

                if (useDatabase != null) {
                    inboundHandler.gameData = OfflineGameData(useDatabase)
                    event.gameProfile = useDatabase.toVelocityGameProfile()
                }
            }
            is moe.caa.multilogin.velocity.auth.OnlineGameData -> {}
        }
    }

    fun handleOfflineLogin(event: LoginEvent, player: Player) {
        val findPlayer = MultiLoginVelocity.instance.proxyServer.getPlayer(player.uniqueId).getOrNull()
        if(findPlayer != null){
            event.result = ResultedEvent.ComponentResult.denied(
                MultiLoginVelocity.instance.message.message("offline_login_failure_reason_already_connected")
                    .replace("{profile_name}", player.username)
            )
            return
        }

        if (player.gameData == null) {
            event.result = ResultedEvent.ComponentResult.denied(
                MultiLoginVelocity.instance.message.message("offline_login_failure_reason_not_found_profile")
                    .replace("{profile_name}", player.username)
            )
            return
        }

        MultiLoginVelocity.instance.logger.info(
            "Offline profile ${player.gameProfile.name}(uuid: ${
                player.gameProfile.id
            }) requests login, approved..."
        )
    }
}