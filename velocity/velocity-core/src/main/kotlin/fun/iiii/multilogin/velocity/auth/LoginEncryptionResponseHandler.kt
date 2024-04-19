package `fun`.iiii.multilogin.velocity.auth

import com.google.common.primitives.Longs
import com.velocitypowered.api.util.GameProfile
import com.velocitypowered.proxy.VelocityServer
import com.velocitypowered.proxy.connection.client.AuthSessionHandler
import com.velocitypowered.proxy.connection.client.InitialLoginSessionHandler
import com.velocitypowered.proxy.connection.client.LoginInboundConnection
import com.velocitypowered.proxy.crypto.EncryptionUtils
import com.velocitypowered.proxy.protocol.packet.EncryptionResponsePacket
import com.velocitypowered.proxy.protocol.packet.ServerLoginPacket
import `fun`.iiii.multilogin.velocity.inject.netty.MultiLoginChannelHandler
import `fun`.iiii.multilogin.velocity.util.*
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
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

        private val AUTH_SESSION_HANDLER_CONSTRUCTOR: MethodHandle

        init {
            val loginStateClass =
                Class.forName("com.velocitypowered.proxy.connection.client.InitialLoginSessionHandler\$LoginState") as Class<Enum<*>>

            LOGIN_STATE_LOGIN_PACKET_EXPECTED = loginStateClass.getEnumConstant("LOGIN_PACKET_EXPECTED")!!
            LOGIN_STATE_LOGIN_PACKET_RECEIVED = loginStateClass.getEnumConstant("LOGIN_PACKET_RECEIVED")!!
            LOGIN_STATE_ENCRYPTION_REQUEST_SENT = loginStateClass.getEnumConstant("ENCRYPTION_REQUEST_SENT")!!
            LOGIN_STATE_ENCRYPTION_RESPONSE_RECEIVED = loginStateClass.getEnumConstant("ENCRYPTION_RESPONSE_RECEIVED")!!

            val lookup = MethodHandles.lookup()
            lookup.unreflectMethodAccess(
                InitialLoginSessionHandler::class.java.getDeclaredMethod(
                    "assertState",
                    loginStateClass
                )
            )

            ASSERT_STATE_METHOD = lookup.unreflectMethodAccess(
                InitialLoginSessionHandler::class.java.getDeclaredMethod(
                    "assertState",
                    loginStateClass
                )
            )
            CURRENT_STATE_GETTER =
                lookup.unreflectFieldGetterAccess(InitialLoginSessionHandler::class.java.getDeclaredField("currentState"))
            CURRENT_STATE_SETTER =
                lookup.unreflectFieldSetterAccess(InitialLoginSessionHandler::class.java.getDeclaredField("currentState"))
            SERVER_LOGIN_PACKET_GETTER =
                lookup.unreflectFieldGetterAccess(InitialLoginSessionHandler::class.java.getDeclaredField("login"))
            VERIFY_GETTER =
                lookup.unreflectFieldGetterAccess(InitialLoginSessionHandler::class.java.getDeclaredField("verify"))
            LOGIN_IN_BOUND_CONNECTION_GETTER =
                lookup.unreflectFieldGetterAccess(InitialLoginSessionHandler::class.java.getDeclaredField("inbound"))

            AUTH_SESSION_HANDLER_CONSTRUCTOR = lookup.unreflectConstructorAccess(
                AuthSessionHandler::class.java.getDeclaredConstructor(
                    VelocityServer::class.java,
                    LoginInboundConnection::class.java,
                    GameProfile::class.java,
                    Boolean::class.java
                )
            )
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
            throw IllegalStateException("No EncryptionRequest packet sent yet.")
        }

        try {
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

//            CompletableFuture.supplyAsync({
//                channelHandler.plugin.multiCore.authenticationHandler.auth(
//                    LoginProfile(
//                        login.username,
//                        EncryptionUtils.generateServerId(decryptedSharedSecret, serverKeyPair.public),
//                        (channelHandler.connection.remoteAddress as InetSocketAddress).hostString
//                    )
//                )
//            }, channelHandler.plugin.multiCore.asyncExecute).whenCompleteAsync({ authenticationResult, throwable ->
//                if (channelHandler.connection.isClosed) return@whenCompleteAsync
//
//                try {
//                    channelHandler.connection.enableEncryption(decryptedSharedSecret)
//                } catch (gse: GeneralSecurityException) {
//                    logError("Unable to enable encryption for connection.", gse)
//                    this.channelHandler.connection.close(true)
//                    return@whenCompleteAsync
//                }
//
//                if (throwable != null) {
//                    logError("An exception was encountered while processing login profile authentication.", throwable)
//
//                    bound.disconnect(language("auth_failed_unknown"))
//                }
//
//                when (authenticationResult) {
//                    is AuthenticationFailureResult -> bound.disconnect(authenticationResult.failureReason)
//                    is AuthenticationSuccessResult -> channelHandler.connection.setActiveSessionHandler(
//                        StateRegistry.LOGIN, AUTH_SESSION_HANDLER_CONSTRUCTOR.invoke(
//                            channelHandler.connection.server,
//                            bound,
//                            GameProfile(
//                                authenticationResult.gameProfile.uuid,
//                                authenticationResult.gameProfile.name,
//                                authenticationResult.gameProfile.properties
//                                    .map { it.second }
//                                    .map {
//                                        GameProfile.Property(
//                                            it.name,
//                                            it.value,
//                                            it.signature
//                                        )
//                                    }
//                            ), true
//                        ) as AuthSessionHandler
//                    )
//                }
//            }, channelHandler.connection.eventLoop())
        } catch (gse: GeneralSecurityException) {
            moe.caa.multilogin.core.util.logError("Unable to enable encryption", gse)
            channelHandler.connection.close(true)
        }
    }
}