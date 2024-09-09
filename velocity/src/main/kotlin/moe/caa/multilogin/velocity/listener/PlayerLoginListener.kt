package moe.caa.multilogin.velocity.listener

import com.velocitypowered.api.event.PostOrder
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.LoginEvent
import com.velocitypowered.api.event.connection.PreLoginEvent
import com.velocitypowered.proxy.connection.client.InitialInboundConnection
import com.velocitypowered.proxy.connection.client.LoginInboundConnection
import moe.caa.multilogin.velocity.auth.OfflineGameData
import moe.caa.multilogin.velocity.auth.OnlineGameData
import moe.caa.multilogin.velocity.main.MultiLoginVelocity
import moe.caa.multilogin.velocity.offline.OfflineLoginHandler
import moe.caa.multilogin.velocity.util.*
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles

object PlayerLoginListener {
    val LOGIN_INBOUND_CONNECTION__DELEGATE_GETTER: MethodHandle

    init {
        MethodHandles.lookup().apply {
            LOGIN_INBOUND_CONNECTION__DELEGATE_GETTER = unreflectGetter(
                LoginInboundConnection::class.java.getDeclaredField("delegate").access()
            )
        }
    }

    fun init() {
        MultiLoginVelocity.instance.proxyServer.eventManager.register(MultiLoginVelocity.instance, this)
    }

    @Subscribe(order = PostOrder.FIRST)
    fun onPreLogin(event: PreLoginEvent) {
        if (!event.result.isAllowed) return

        val connection = event.connection
        if (connection is LoginInboundConnection) {
            val inboundHandler =
                (LOGIN_INBOUND_CONNECTION__DELEGATE_GETTER.invoke(connection) as InitialInboundConnection)
                    .connection.channel.getMultiLoginInboundHandler()
            if (inboundHandler.shouldForceOffline) {
                event.result = PreLoginEvent.PreLoginComponentResult.forceOfflineMode()
                return
            }
        }
        event.result = PreLoginEvent.PreLoginComponentResult.forceOnlineMode()
    }

    @Subscribe
    fun onLogin(event: LoginEvent) {
        when (val data =  event.player.gameData) {
            is OfflineGameData, null -> OfflineLoginHandler.handleOfflineLogin(event, event.player)
            is OnlineGameData -> {
                MultiLoginVelocity.instance.logger.info(
                    "${data.userProfile.username}(uuid: ${
                        data.userProfile.uuid
                    }) from authentication service ${
                        data.service.baseServiceSetting.serviceName
                    }(service id: ${
                        data.service.baseServiceSetting.serviceId
                    }) has been authenticated, profile redirected to ${
                        data.inGameProfile.username
                    }(uuid: ${
                        data.inGameProfile.uuid
                    })"
                )
            }
        }
    }
}