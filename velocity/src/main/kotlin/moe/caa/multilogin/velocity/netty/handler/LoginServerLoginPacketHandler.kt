package moe.caa.multilogin.velocity.netty.handler

import com.velocitypowered.proxy.connection.client.InitialLoginSessionHandler
import com.velocitypowered.proxy.protocol.packet.ServerLoginPacket
import moe.caa.multilogin.velocity.main.MultiLoginVelocity
import moe.caa.multilogin.velocity.netty.ChannelInboundHandler
import moe.caa.multilogin.velocity.util.access
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles

class LoginServerLoginPacketHandler(
    private val loginSessionHandler: InitialLoginSessionHandler,
    private val channelHandler: ChannelInboundHandler
) {
    companion object {
        private val SERVER_LOGIN_PACKET_USERNAME_SETTER: MethodHandle
        private val SERVER_LOGIN_PACKET_PLAYER_KEY_SETTER: MethodHandle
        private val SERVER_LOGIN_PACKET_HOLDER_UUID_SETTER: MethodHandle

        init {
            MethodHandles.lookup().apply {
                SERVER_LOGIN_PACKET_USERNAME_SETTER =
                    unreflectSetter(ServerLoginPacket::class.java.getDeclaredField("username").access())
                SERVER_LOGIN_PACKET_PLAYER_KEY_SETTER =
                    unreflectSetter(ServerLoginPacket::class.java.getDeclaredField("playerKey").access())
                SERVER_LOGIN_PACKET_HOLDER_UUID_SETTER =
                    unreflectSetter(ServerLoginPacket::class.java.getDeclaredField("holderUuid").access())
            }
        }
    }


    fun handle(packet: ServerLoginPacket) {
        if (!MultiLoginVelocity.instance.config.offlineAuthSetting.enable) {
            loginSessionHandler.handle(packet)
            return
        }

        var loginName = packet.username
        if (MultiLoginVelocity.instance.config.offlineAuthSetting.bindHosts.any {
                it.equals(channelHandler.clientConnectAddress, ignoreCase = true)
            }) {
            channelHandler.shouldForceOffline = true
        }

        if (MultiLoginVelocity.instance.config.offlineAuthSetting.chooseProfileNameFromHostSetting.enable) {
            val chooseName =
                MultiLoginVelocity.instance.config.offlineAuthSetting.chooseProfileNameFromHostSetting.chooseNameOrNull(
                    channelHandler.clientConnectAddress
                )

            if (chooseName != null) {
                channelHandler.shouldForceOffline = true
                loginName = chooseName
                MultiLoginVelocity.instance.logDebug("Select $chooseName from ${channelHandler.clientConnectAddress}")
            }
        }

        loginSessionHandler.handle(ServerLoginPacket().apply {
            SERVER_LOGIN_PACKET_USERNAME_SETTER.invoke(this, loginName)
            SERVER_LOGIN_PACKET_PLAYER_KEY_SETTER.invoke(this, packet.playerKey)
            SERVER_LOGIN_PACKET_HOLDER_UUID_SETTER.invoke(this, packet.holderUuid)
        })
    }
}