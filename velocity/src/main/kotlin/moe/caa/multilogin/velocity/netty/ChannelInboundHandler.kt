package moe.caa.multilogin.velocity.netty

import com.velocitypowered.proxy.connection.MinecraftConnection
import com.velocitypowered.proxy.connection.client.HandshakeSessionHandler
import com.velocitypowered.proxy.connection.client.InitialLoginSessionHandler
import com.velocitypowered.proxy.protocol.packet.EncryptionResponsePacket
import com.velocitypowered.proxy.protocol.packet.HandshakePacket
import com.velocitypowered.proxy.protocol.packet.ServerLoginPacket
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import moe.caa.multilogin.velocity.auth.GameData
import moe.caa.multilogin.velocity.main.MultiLoginVelocity
import moe.caa.multilogin.velocity.netty.handler.LoginEncryptionResponsePacketHandler
import moe.caa.multilogin.velocity.netty.handler.LoginServerLoginPacketHandler

class ChannelInboundHandler(
    val plugin: MultiLoginVelocity,
    val connection: MinecraftConnection
) : ChannelInboundHandlerAdapter() {
    var clientConnectAddress: String? = null
    var shouldForceOffline = false

    var gameData: GameData? = null

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        val activeSessionHandler = connection.activeSessionHandler
        if (activeSessionHandler is InitialLoginSessionHandler) {
            if (msg is EncryptionResponsePacket) {
                LoginEncryptionResponsePacketHandler(activeSessionHandler, this).handle(msg)
                return
            }
            if (msg is ServerLoginPacket) {
                LoginServerLoginPacketHandler(activeSessionHandler, this).handle(msg)
                return
            }
        }
        if (activeSessionHandler is HandshakeSessionHandler) {
            if (msg is HandshakePacket) {
                clientConnectAddress = msg.serverAddress
            }
        }
        ctx.fireChannelRead(msg)
    }
}