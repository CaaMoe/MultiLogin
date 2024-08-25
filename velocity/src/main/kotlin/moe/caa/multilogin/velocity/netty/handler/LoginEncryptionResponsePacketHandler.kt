package moe.caa.multilogin.velocity.netty.handler

import com.google.common.primitives.Longs
import com.velocitypowered.proxy.connection.client.InitialLoginSessionHandler
import com.velocitypowered.proxy.connection.client.LoginInboundConnection
import com.velocitypowered.proxy.crypto.EncryptionUtils
import com.velocitypowered.proxy.protocol.packet.EncryptionResponsePacket
import com.velocitypowered.proxy.protocol.packet.ServerLoginPacket
import kotlinx.coroutines.*
import moe.caa.multilogin.velocity.auth.yggdrasil.YggdrasilAuthenticationResult
import moe.caa.multilogin.velocity.netty.MultiLoginChannelHandler
import moe.caa.multilogin.velocity.util.access
import moe.caa.multilogin.velocity.util.enumConstant
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.net.InetSocketAddress
import java.security.GeneralSecurityException
import java.security.MessageDigest

class LoginEncryptionResponsePacketHandler(
    private val loginSessionHandler: InitialLoginSessionHandler,
    private val channelHandler: MultiLoginChannelHandler
) {
    companion object {
        private val LOGIN_PACKET_EXPECTED: Any
        private val LOGIN_PACKET_RECEIVED: Any
        private val ENCRYPTION_REQUEST_SENT: Any
        private val ENCRYPTION_RESPONSE_RECEIVED: Any

        private val CURRENT_STATE_GETTER: MethodHandle
        private val CURRENT_STATE_SETTER: MethodHandle
        private val ASSERT_STATE_METHOD: MethodHandle

        private val INBOUND_GETTER: MethodHandle
        private val LOGIN_GETTER: MethodHandle
        private val VERIFY_GETTER: MethodHandle


        init {
            val loginStateClass =
                Class.forName("com.velocitypowered.proxy.connection.client.InitialLoginSessionHandler\$LoginState")
                    .apply {
                        LOGIN_PACKET_EXPECTED = enumConstant("LOGIN_PACKET_EXPECTED")
                        LOGIN_PACKET_RECEIVED = enumConstant("LOGIN_PACKET_RECEIVED")
                        ENCRYPTION_REQUEST_SENT = enumConstant("ENCRYPTION_REQUEST_SENT")
                        ENCRYPTION_RESPONSE_RECEIVED = enumConstant("ENCRYPTION_RESPONSE_RECEIVED")
                    }

            MethodHandles.lookup().apply {
                CURRENT_STATE_GETTER =
                    unreflectGetter(InitialLoginSessionHandler::class.java.getDeclaredField("currentState").access())
                CURRENT_STATE_SETTER =
                    unreflectSetter(InitialLoginSessionHandler::class.java.getDeclaredField("currentState").access())
                ASSERT_STATE_METHOD = unreflect(
                    InitialLoginSessionHandler::class.java.getDeclaredMethod("assertState", loginStateClass).access()
                )

                INBOUND_GETTER =
                    unreflectGetter(InitialLoginSessionHandler::class.java.getDeclaredField("inbound").access())
                LOGIN_GETTER =
                    unreflectGetter(InitialLoginSessionHandler::class.java.getDeclaredField("login").access())
                VERIFY_GETTER =
                    unreflectGetter(InitialLoginSessionHandler::class.java.getDeclaredField("verify").access())

            }
        }
    }

    private val mcConnection = channelHandler.connection
    private val server = channelHandler.connection.server

    private var inbound: Lazy<LoginInboundConnection> = lazy {
        return@lazy INBOUND_GETTER.invoke(loginSessionHandler) as LoginInboundConnection
    }

    private var login: Lazy<ServerLoginPacket?> = lazy {
        return@lazy LOGIN_GETTER.invoke(loginSessionHandler) as ServerLoginPacket
    }

    private var verify: Lazy<ByteArray> = lazy {
        return@lazy VERIFY_GETTER.invoke(loginSessionHandler) as ByteArray
    }

    fun handle(packet: EncryptionResponsePacket) {
        checkState()
        val loginPacket = login.value ?: throw IllegalArgumentException("No ServerLogin packet received yet.")

        if (verify.value.isEmpty()) {
            throw IllegalStateException("No EncryptionRequest packet sent yet.")
        }

        try {
            val serverKeyPair = server.serverKeyPair

            var decryptedSharedSecret: ByteArray
            val playerKey = inbound.value.identifiedKey
            if (playerKey != null) {
                check(
                    playerKey.verifyDataSignature(
                        packet.verifyToken,
                        *arrayOf<ByteArray>(verify.value, Longs.toByteArray(packet.salt))
                    )
                ) { "Invalid client public signature." }
            } else {
                decryptedSharedSecret = EncryptionUtils.decryptRsa(serverKeyPair, packet.verifyToken)
                check(
                    MessageDigest.isEqual(
                        verify.value,
                        decryptedSharedSecret
                    )
                ) { "Unable to successfully decrypt the verification token." }
            }

            decryptedSharedSecret = EncryptionUtils.decryptRsa(serverKeyPair, packet.sharedSecret)

            val username = loginPacket.username
            val serverId = EncryptionUtils.generateServerId(decryptedSharedSecret, serverKeyPair.public)
            val playerIp = (this.mcConnection.remoteAddress as InetSocketAddress).hostString

            handleAuth(username, serverId, playerIp)

        } catch (gse: GeneralSecurityException) {
            channelHandler.plugin.logger.error("Unable to enable encryption.", gse);
            channelHandler.connection.close(true)
        }
        return
    }

    private fun checkState() {
        ASSERT_STATE_METHOD.invoke(loginSessionHandler, ENCRYPTION_REQUEST_SENT)
        CURRENT_STATE_SETTER.invoke(loginSessionHandler, ENCRYPTION_RESPONSE_RECEIVED)
    }

    private fun handleAuth(username: String, serverId: String, playerIp: String) {
        channelHandler.plugin.asyncExecutor.execute {
            val result = runBlocking {
                channelHandler.plugin.yggdrasilAuthenticationHandler.authenticate(username, serverId, playerIp)
            }
            when (result) {
                is YggdrasilAuthenticationResult.Failure -> TODO()
                is YggdrasilAuthenticationResult.Success -> TODO()
            }
        }
    }
}