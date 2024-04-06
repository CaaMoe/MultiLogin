package `fun`.iiii.multilogin.velocity.auth

import com.google.common.primitives.Longs
import com.velocitypowered.proxy.connection.client.InitialLoginSessionHandler
import com.velocitypowered.proxy.connection.client.LoginInboundConnection
import com.velocitypowered.proxy.crypto.EncryptionUtils
import com.velocitypowered.proxy.protocol.packet.EncryptionResponsePacket
import com.velocitypowered.proxy.protocol.packet.ServerLoginPacket
import `fun`.iiii.multilogin.velocity.inject.netty.MultiLoginChannelHandler
import moe.caa.multilogin.api.logger.logDebug
import moe.caa.multilogin.api.logger.logError
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.net.InetSocketAddress
import java.security.GeneralSecurityException
import java.security.MessageDigest

class LoginEncryptionResponseHandler(private val channelHandler: MultiLoginChannelHandler) {
    companion object {
        private val LOGIN_STATE_LOGIN_PACKET_EXPECTED: Enum<*>
        private val LOGIN_STATE_LOGIN_PACKET_RECEIVED: Enum<*>
        private val LOGIN_STATE_ENCRYPTION_REQUEST_SENT: Enum<*>
        private val LOGIN_STATE_ENCRYPTION_RESPONSE_RECEIVED: Enum<*>

        private val CURRENT_STATE_GETTER: MethodHandle
        private val CURRENT_STATE_SETTER: MethodHandle
        private val ASSERT_STATE_METHOD: MethodHandle
        private val SERVER_LOGIN_PACKET_GETTER: MethodHandle
        private val VERIFY_GETTER: MethodHandle
        private val LOGIN_IN_BOUND_CONNECTION_GETTER: MethodHandle

        init {
            val loginStateClass =
                Class.forName("com.velocitypowered.proxy.connection.client.InitialLoginSessionHandler\$LoginState")
            val loginStateEnumValues = loginStateClass.enumConstants

            LOGIN_STATE_LOGIN_PACKET_EXPECTED = (loginStateEnumValues.find {
                return@find (it as Enum<*>).name == "LOGIN_PACKET_EXPECTED"
            } as Enum<*>)

            LOGIN_STATE_LOGIN_PACKET_RECEIVED = (loginStateEnumValues.find {
                return@find (it as Enum<*>).name == "LOGIN_PACKET_RECEIVED"
            } as Enum<*>)

            LOGIN_STATE_ENCRYPTION_REQUEST_SENT = (loginStateEnumValues.find {
                return@find (it as Enum<*>).name == "ENCRYPTION_REQUEST_SENT"
            } as Enum<*>)

            LOGIN_STATE_ENCRYPTION_RESPONSE_RECEIVED = (loginStateEnumValues.find {
                return@find (it as Enum<*>).name == "ENCRYPTION_RESPONSE_RECEIVED"
            } as Enum<*>)

            val lookup = MethodHandles.lookup()
            ASSERT_STATE_METHOD = InitialLoginSessionHandler::class.java.let {
                lookup.unreflect(
                    it.getDeclaredMethod("assertState", loginStateClass).apply {
                        isAccessible = true
                    })
            }

            CURRENT_STATE_GETTER = InitialLoginSessionHandler::class.java.let {
                lookup.unreflectGetter(
                    it.getDeclaredField("currentState").apply {
                        isAccessible = true
                    })
            }

            CURRENT_STATE_SETTER = InitialLoginSessionHandler::class.java.let {
                lookup.unreflectSetter(
                    it.getDeclaredField("currentState").apply {
                        isAccessible = true
                    })
            }

            SERVER_LOGIN_PACKET_GETTER = InitialLoginSessionHandler::class.java.let {
                lookup.unreflectGetter(
                    it.getDeclaredField("login").apply {
                        isAccessible = true
                    })
            }

            VERIFY_GETTER = InitialLoginSessionHandler::class.java.let {
                lookup.unreflectGetter(
                    it.getDeclaredField("verify").apply {
                        isAccessible = true
                    })
            }

            LOGIN_IN_BOUND_CONNECTION_GETTER = InitialLoginSessionHandler::class.java.let {
                lookup.unreflectGetter(
                    it.getDeclaredField("inbound").apply {
                        isAccessible = true
                    })
            }
        }
    }


    fun handleEncryptionResponsePacket(packet: EncryptionResponsePacket) {
        val initialLoginSessionHandler = channelHandler.connection.activeSessionHandler as InitialLoginSessionHandler

        ASSERT_STATE_METHOD.invoke(initialLoginSessionHandler, LOGIN_STATE_ENCRYPTION_REQUEST_SENT)
        CURRENT_STATE_SETTER.invoke(initialLoginSessionHandler, LOGIN_STATE_LOGIN_PACKET_RECEIVED)
        val login = SERVER_LOGIN_PACKET_GETTER.invoke(initialLoginSessionHandler) as ServerLoginPacket?
            ?: throw IllegalStateException("No ServerLogin packet received yet.")

        val verify = VERIFY_GETTER.invoke(initialLoginSessionHandler) as ByteArray
        if (verify.isEmpty()) {
            throw java.lang.IllegalStateException("No EncryptionRequest packet sent yet.")
        }

        kotlin.runCatching {
            val serverKeyPair = channelHandler.connection.server.serverKeyPair
            var decryptedSharedSecret: ByteArray
            val bound = LOGIN_IN_BOUND_CONNECTION_GETTER.invoke(initialLoginSessionHandler) as LoginInboundConnection
            if (bound.identifiedKey != null) {
                check(
                    bound.identifiedKey!!.verifyDataSignature(
                        packet.verifyToken,
                        *arrayOf(verify, Longs.toByteArray(packet.salt))
                    )
                ) { "Invalid client public signature." }
            } else {
                decryptedSharedSecret = EncryptionUtils.decryptRsa(serverKeyPair, packet.verifyToken)
                check(
                    MessageDigest.isEqual(verify, decryptedSharedSecret)
                ) { "Unable to successfully decrypt the verification token." }
            }

            decryptedSharedSecret = EncryptionUtils.decryptRsa(serverKeyPair, packet.sharedSecret)

            val username = login.username
            val serverId = EncryptionUtils.generateServerId(decryptedSharedSecret, serverKeyPair.public)
            val playerIp = (channelHandler.connection.remoteAddress as InetSocketAddress).hostString

            logDebug("username = $username, serverId = $serverId, playerIp = $playerIp")

        }.onFailure {
            if (it is GeneralSecurityException) {
                logError("Unable to enable encryption", it)
                channelHandler.connection.close(true)
            }
        }
    }
}