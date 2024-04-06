package `fun`.iiii.multilogin.velocity.util

import com.velocitypowered.api.proxy.Player
import com.velocitypowered.proxy.connection.client.ConnectedPlayer
import `fun`.iiii.multilogin.velocity.inject.netty.MultiLoginChannelInitializer

fun Player.getMultiLoginData() {
    this as ConnectedPlayer
    connection.channel.pipeline()[MultiLoginChannelInitializer.MULTI_LOGIN_HANDLE_NAME]
}