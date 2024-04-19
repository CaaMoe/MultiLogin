package `fun`.iiii.multilogin.velocity.inject.netty

import com.velocitypowered.proxy.connection.MinecraftConnection
import com.velocitypowered.proxy.connection.client.InitialLoginSessionHandler
import com.velocitypowered.proxy.protocol.packet.EncryptionResponsePacket
import `fun`.iiii.multilogin.velocity.auth.LoginEncryptionResponseHandler
import `fun`.iiii.multilogin.velocity.main.MultiLoginVelocityCore
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter

class MultiLoginChannelHandler(val plugin: MultiLoginVelocityCore, val connection: MinecraftConnection) :
    ChannelInboundHandlerAdapter() {
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if (connection.activeSessionHandler is InitialLoginSessionHandler) {
            if (msg is EncryptionResponsePacket) {
                LoginEncryptionResponseHandler(this).handleEncryptionResponsePacket(msg)
                return
            }
        }
        ctx.fireChannelRead(msg)
    }
}