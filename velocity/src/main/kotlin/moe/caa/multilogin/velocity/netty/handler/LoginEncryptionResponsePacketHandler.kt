package moe.caa.multilogin.velocity.netty.handler

import com.google.common.primitives.Longs
import com.velocitypowered.api.proxy.crypto.IdentifiedKey
import com.velocitypowered.api.util.GameProfile
import com.velocitypowered.proxy.VelocityServer
import com.velocitypowered.proxy.connection.client.AuthSessionHandler
import com.velocitypowered.proxy.connection.client.InitialLoginSessionHandler
import com.velocitypowered.proxy.connection.client.LoginInboundConnection
import com.velocitypowered.proxy.crypto.EncryptionUtils
import com.velocitypowered.proxy.crypto.IdentifiedKeyImpl
import com.velocitypowered.proxy.protocol.StateRegistry
import com.velocitypowered.proxy.protocol.packet.EncryptionResponsePacket
import com.velocitypowered.proxy.protocol.packet.ServerLoginPacket
import kotlinx.coroutines.*
import moe.caa.multilogin.velocity.auth.validate.ValidateResult
import moe.caa.multilogin.velocity.auth.yggdrasil.LoginProfile
import moe.caa.multilogin.velocity.auth.yggdrasil.YggdrasilAuthenticationResult
import moe.caa.multilogin.velocity.auth.yggdrasil.YggdrasilAuthenticationResult.Failure
import moe.caa.multilogin.velocity.auth.yggdrasil.YggdrasilAuthenticationResult.Success
import moe.caa.multilogin.velocity.netty.ChannelInboundHandler
import moe.caa.multilogin.velocity.util.access
import moe.caa.multilogin.velocity.util.enumConstant
import moe.caa.multilogin.velocity.util.toVelocityGameProfile
import net.kyori.adventure.text.Component
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.net.InetSocketAddress
import java.security.GeneralSecurityException
import java.security.MessageDigest
import java.util.concurrent.Callable

class LoginEncryptionResponsePacketHandler(
    private val loginSessionHandler: InitialLoginSessionHandler,
    private val channelHandler: ChannelInboundHandler
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

        private val AUTH_SESSION_HANDLER_CONSTRUCTOR: MethodHandle

        private val authScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

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

                AUTH_SESSION_HANDLER_CONSTRUCTOR =
                    unreflectConstructor(
                        AuthSessionHandler::class.java.getDeclaredConstructor(
                            VelocityServer::class.java,
                            LoginInboundConnection::class.java,
                            GameProfile::class.java,
                            Boolean::class.java
                        ).access()
                    )
            }
        }

        fun close() {
            authScope.cancel()
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

    private lateinit var finalDecryptedSharedSecret: ByteArray

    var handleEncrypted: Boolean = false;

    fun handle(packet: EncryptionResponsePacket) {
        checkState()
        val loginPacket = login.value ?: throw IllegalArgumentException("No ServerLogin packet received yet.")

        if (verify.value.isEmpty()) {
            throw IllegalStateException("No EncryptionRequest packet sent yet.")
        }

        try {
            val serverKeyPair = server.serverKeyPair

            val playerKey = inbound.value.identifiedKey
            if (playerKey != null) {
                check(
                    playerKey.verifyDataSignature(
                        packet.verifyToken,
                        *arrayOf<ByteArray>(verify.value, Longs.toByteArray(packet.salt))
                    )
                ) { "Invalid client public signature." }
            } else {
                check(
                    MessageDigest.isEqual(
                        verify.value,
                        EncryptionUtils.decryptRsa(serverKeyPair, packet.verifyToken)
                    )
                ) { "Unable to successfully decrypt the verification token." }
            }

            finalDecryptedSharedSecret = EncryptionUtils.decryptRsa(serverKeyPair, packet.sharedSecret)

            val username = loginPacket.username
            val serverId = EncryptionUtils.generateServerId(finalDecryptedSharedSecret, serverKeyPair.public)
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
        authScope.launch(Dispatchers.IO) {
            val loginProfile = LoginProfile(username, serverId, playerIp)
            try {
                handleAuthResult(channelHandler.plugin.yggdrasilAuthenticationHandler.auth(loginProfile))
            } catch (t: Throwable) {
                channelHandler.plugin.logger.error(
                    "An exception occurred while verifying the $loginProfile session.",
                    t
                )
                encryptConnection()
                inbound.value.disconnect(channelHandler.plugin.message.message("authentication_yggdrasil_failure_reason_error"))
            }
        }
    }

    // 加密, 并且返回是否已经加密
    // 如果加密失败或者客户端已断开连接就返回 false
    private fun encryptConnection(): Boolean {
        if (handleEncrypted) return true

        return channelHandler.connection.eventLoop().submit(object : Callable<Boolean> {
            override fun call(): Boolean {
                if (mcConnection.isClosed) return false

                synchronized(mcConnection) {
                    if (handleEncrypted) return true
                    try {
                        mcConnection.enableEncryption(finalDecryptedSharedSecret)
                        handleEncrypted = true
                        return true
                    } catch (gse: GeneralSecurityException) {
                        channelHandler.plugin.logger.error("Unable to enable encryption for connection.", gse);
                        channelHandler.connection.close(true)
                        return false
                    }
                }
            }
        }).get()
    }

    private fun loginSucceed(profile: moe.caa.multilogin.api.profile.GameProfile) {
        channelHandler.connection.eventLoop().submit(Callable {
            mcConnection.setActiveSessionHandler(
                StateRegistry.LOGIN, AUTH_SESSION_HANDLER_CONSTRUCTOR.invoke(
                    server,
                    inbound.value,
                    profile.toVelocityGameProfile(),
                    true
                ) as AuthSessionHandler
            )
        }).get()
    }

    private fun handleAuthResult(result: YggdrasilAuthenticationResult) {
        if (encryptConnection()) {
            when (result) {
                is Failure -> when (result.reason) {
                    Failure.Reason.NO_YGGDRASIL_SERVICES -> inbound.value.disconnect(
                        channelHandler.plugin.message.message(
                            "authentication_yggdrasil_failure_reason_no_yggdrasil_services"
                        )
                    )

                    Failure.Reason.INVALID_SESSION -> inbound.value.disconnect(channelHandler.plugin.message.message("authentication_yggdrasil_failure_reason_invalid_session"))
                    Failure.Reason.SERVER_BREAK_DOWN -> inbound.value.disconnect(channelHandler.plugin.message.message("authentication_yggdrasil_failure_reason_server_break_down"))
                }

                is Success -> {
                    val identifiedKey = inbound.value.identifiedKey
                    if (identifiedKey != null) {
                        if (identifiedKey.keyRevision == IdentifiedKey.Revision.LINKED_V2) {
                            if (identifiedKey is IdentifiedKeyImpl) {
                                if (!identifiedKey.internalAddHolder(result.profile.uuid)) {
                                    inbound.value.disconnect(Component.translatable("multiplayer.disconnect.invalid_public_key"))
                                    return
                                }
                            }
                        }
                    }

                    val validateContext = result.buildValidateContext()
                    when (val validateResult =
                        channelHandler.plugin.validateAuthenticationHandler.checkIn(validateContext)) {
                        is ValidateResult.Failure -> inbound.value.disconnect(validateResult.reason)
                        is ValidateResult.Pass -> {
                            channelHandler.gameData = validateContext.toGameData()
                            loginSucceed(validateContext.profileGameProfile)
                        }
                    }
                }
            }
        }
    }
}