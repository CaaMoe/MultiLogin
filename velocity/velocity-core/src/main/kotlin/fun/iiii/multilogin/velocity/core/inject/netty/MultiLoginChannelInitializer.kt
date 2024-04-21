package `fun`.iiii.multilogin.velocity.core.inject.netty

import com.velocitypowered.proxy.connection.MinecraftConnection
import com.velocitypowered.proxy.network.ConnectionManager
import `fun`.iiii.multilogin.velocity.core.main.MultiLoginVelocityCore
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles

class MultiLoginChannelInitializer(
    private val plugin: MultiLoginVelocityCore,
    private val originChannel: ChannelInitializer<Channel>
) : ChannelInitializer<Channel>() {

    companion object {
        const val MULTI_LOGIN_HANDLE_NAME = "multilogin-handler"
        private val INIT_CHANNEL_METHOD_HANDLER: MethodHandle;

        init {
            val methodLookup = MethodHandles.lookup()
            INIT_CHANNEL_METHOD_HANDLER = methodLookup.unreflect(
                ChannelInitializer::class.java.getDeclaredMethod("initChannel", Channel::class.java).apply {
                    isAccessible = true
                }
            )
        }

        fun init(plugin: MultiLoginVelocityCore) {
            val connectionManager: ConnectionManager = Class.forName("com.velocitypowered.proxy.VelocityServer")
                .getDeclaredField("cm").apply {
                    isAccessible = true
                }.let {
                    it.get(plugin.bootstrap.proxyServer) as ConnectionManager
                }

            val serverChannelInitializerHolder = connectionManager.getServerChannelInitializer()
            serverChannelInitializerHolder.set(
                MultiLoginChannelInitializer(plugin, serverChannelInitializerHolder.get())
            )
        }
    }


    override fun initChannel(channel: Channel) {
        INIT_CHANNEL_METHOD_HANDLER.invoke(originChannel, channel)

        channel.pipeline().addBefore(
            "handler", MULTI_LOGIN_HANDLE_NAME, MultiLoginChannelHandler(
                plugin,
                channel.pipeline().get("handler") as MinecraftConnection
            )
        )
    }
}