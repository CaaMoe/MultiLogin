package moe.caa.multilogin.velocity.inject

import com.velocitypowered.proxy.connection.MinecraftConnection
import com.velocitypowered.proxy.network.ConnectionManager
import com.velocitypowered.proxy.network.ServerChannelInitializerHolder
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import moe.caa.multilogin.velocity.util.access
import moe.caa.multilogin.velocity.main.MultiLoginVelocity
import moe.caa.multilogin.velocity.netty.MultiLoginChannelHandler
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles

/**
 * 往 Channel 挂 Handler
 */
class VelocityServerChannelInitializerInjector(
    val plugin: MultiLoginVelocity
) {

    companion object {
        // handler 名字
        const val MULTI_LOGIN_PACKET_HANDLER_NAME = "multilogin-handler"

        // io.netty.channel.ChannelInitializer.initChannel(Channel)
        private val INIT_CHANNEL_METHOD_HANDLER: MethodHandle

        // com.velocitypowered.proxy.network.ServerChannelInitializerHolder.initializer
        private val SERVER_CHANNEL_INITIALIZER_HOLDER_INITIALIZER_FIELD_SETTER: MethodHandle

        init {
            MethodHandles.lookup().apply {
                INIT_CHANNEL_METHOD_HANDLER = unreflect(
                    ChannelInitializer::class.java.getDeclaredMethod("initChannel", Channel::class.java).access()
                )

                SERVER_CHANNEL_INITIALIZER_HOLDER_INITIALIZER_FIELD_SETTER = unreflectSetter(
                    ServerChannelInitializerHolder::class.java.getDeclaredField("initializer").access()
                )
            }
        }
    }

    // 往 Initializer 加点东西
    fun inject() {
        // com.velocitypowered.proxy.VelocityServer.cm
        val connectionManager = plugin.proxyServer::class.java.getDeclaredField("cm")
            .access()
            .get(plugin.proxyServer) as ConnectionManager

        val serverChannelInitializer = connectionManager.getServerChannelInitializer()
        SERVER_CHANNEL_INITIALIZER_HOLDER_INITIALIZER_FIELD_SETTER.invoke(
            serverChannelInitializer,
            ProxyChannelInitializer(serverChannelInitializer.get())
        )
//        serverChannelInitializer.set(ProxyChannelInitializer(serverChannelInitializer.get()))
    }

    private fun attach(channel: Channel) {
        val connection = channel.pipeline().get("handler") as MinecraftConnection

        channel.pipeline().addBefore(
            "handler", MULTI_LOGIN_PACKET_HANDLER_NAME,
            MultiLoginChannelHandler(plugin, connection)
        )
    }

    inner class ProxyChannelInitializer(
        private val vanillaChannelInitializer: ChannelInitializer<Channel>,
    ) : ChannelInitializer<Channel>() {
        override fun initChannel(channel: Channel) {
            INIT_CHANNEL_METHOD_HANDLER.invoke(vanillaChannelInitializer, channel)

            attach(channel)
        }
    }
}