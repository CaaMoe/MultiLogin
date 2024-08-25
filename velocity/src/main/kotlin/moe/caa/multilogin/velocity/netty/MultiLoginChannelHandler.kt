package moe.caa.multilogin.velocity.netty

import com.velocitypowered.proxy.connection.MinecraftConnection
import com.velocitypowered.proxy.connection.client.InitialLoginSessionHandler
import com.velocitypowered.proxy.protocol.packet.EncryptionResponsePacket
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import moe.caa.multilogin.velocity.main.MultiLoginVelocity
import moe.caa.multilogin.velocity.netty.handler.LoginEncryptionResponsePacketHandler

class MultiLoginChannelHandler(
    val plugin: MultiLoginVelocity,
    val connection: MinecraftConnection
) : ChannelInboundHandlerAdapter() {

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        val activeSessionHandler = connection.activeSessionHandler
        if (activeSessionHandler is InitialLoginSessionHandler) {
            if (msg is EncryptionResponsePacket) {
                LoginEncryptionResponsePacketHandler(activeSessionHandler, this).handle(msg)
                return
            }
        }

        ctx.fireChannelRead(msg)
    }
}