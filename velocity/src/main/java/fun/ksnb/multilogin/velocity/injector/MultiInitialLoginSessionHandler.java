package fun.ksnb.multilogin.velocity.injector;

import com.google.common.primitives.Longs;
import com.velocitypowered.api.proxy.crypto.IdentifiedKey;
import com.velocitypowered.api.util.GameProfile;
import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.connection.MinecraftConnection;
import com.velocitypowered.proxy.connection.client.AuthSessionHandler;
import com.velocitypowered.proxy.connection.client.InitialLoginSessionHandler;
import com.velocitypowered.proxy.connection.client.LoginInboundConnection;
import com.velocitypowered.proxy.crypto.EncryptionUtils;
import com.velocitypowered.proxy.protocol.packet.EncryptionResponse;
import com.velocitypowered.proxy.protocol.packet.ServerLogin;
import lombok.AccessLevel;
import lombok.Getter;
import moe.caa.multilogin.api.auth.AuthResult;
import moe.caa.multilogin.api.auth.yggdrasil.response.HasJoinedResponse;
import moe.caa.multilogin.api.logger.LoggerProvider;
import moe.caa.multilogin.api.main.MultiCoreAPI;
import moe.caa.multilogin.api.util.ReflectUtil;
import net.kyori.adventure.text.Component;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.net.InetSocketAddress;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 接管 InitialLoginSessionHandler 类的其中一个方法
 */
@Getter(value = AccessLevel.PROTECTED)
public class MultiInitialLoginSessionHandler {

    // LoginStateEnum 的枚举
    private static Enum<?> initialLoginSessionHandler$loginStateEnum$LOGIN_PACKET_EXPECTED;
    private static Enum<?> initialLoginSessionHandler$loginStateEnum$LOGIN_PACKET_RECEIVED;
    private static Enum<?> initialLoginSessionHandler$loginStateEnum$ENCRYPTION_REQUEST_SENT;
    private static Enum<?> initialLoginSessionHandler$loginStateEnum$ENCRYPTION_RESPONSE_RECEIVED;

    // 一些函数和字段的引用
    private static MethodHandle initialLoginSessionHandler_assertStateMethod;
    private static MethodHandle initialLoginSessionHandler_setCurrentStateField;
    private static MethodHandle initialLoginSessionHandler_getLoginField;
    private static MethodHandle initialLoginSessionHandler_getVerifyField;
    private static MethodHandle initialLoginSessionHandler_getServerField;
    private static MethodHandle initialLoginSessionHandler_getInboundField;
    private static MethodHandle initialLoginSessionHandler_getMcConnectionField;
    private static MethodHandle initialLoginSessionHandler_getCurrentStateField;
    private static MethodHandle authSessionHandler_allArgsConstructor;
    // 类体常量
    private final InitialLoginSessionHandler initialLoginSessionHandler;
    private final MultiCoreAPI multiCoreAPI; // 这个不是
    private final VelocityServer server;
    private final MinecraftConnection mcConnection;
    private final LoginInboundConnection inbound;
    // 运行时改动的实例
    private ServerLogin login;
    private byte[] verify;
    // 自己的对象，表示是否通过加密
    private boolean encrypted = false;

    protected MultiInitialLoginSessionHandler(InitialLoginSessionHandler initialLoginSessionHandler, MultiCoreAPI multiCoreAPI) {
        this.initialLoginSessionHandler = initialLoginSessionHandler;
        this.multiCoreAPI = multiCoreAPI;
        try {
            this.server = (VelocityServer) initialLoginSessionHandler_getServerField.invoke(initialLoginSessionHandler);
            this.mcConnection = (MinecraftConnection) initialLoginSessionHandler_getMcConnectionField.invoke(initialLoginSessionHandler);
            this.inbound = (LoginInboundConnection) initialLoginSessionHandler_getInboundField.invoke(initialLoginSessionHandler);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void init() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, NoSuchFieldException {
        Class<InitialLoginSessionHandler> initialLoginSessionHandlerClass = InitialLoginSessionHandler.class;
        Class<?> initialLoginSessionHandler$loginStateEnum = Class.forName("com.velocitypowered.proxy.connection.client.InitialLoginSessionHandler$LoginState");

        // 获取枚举常量
        for (Object constant : initialLoginSessionHandler$loginStateEnum.getEnumConstants()) {
            final Enum<?> enumObject = (Enum<?>) constant;
            switch ((enumObject).name()) {
                case "LOGIN_PACKET_EXPECTED":
                    initialLoginSessionHandler$loginStateEnum$LOGIN_PACKET_EXPECTED = enumObject;
                    break;
                case "LOGIN_PACKET_RECEIVED":
                    initialLoginSessionHandler$loginStateEnum$LOGIN_PACKET_RECEIVED = enumObject;
                    break;
                case "ENCRYPTION_REQUEST_SENT":
                    initialLoginSessionHandler$loginStateEnum$ENCRYPTION_REQUEST_SENT = enumObject;
                    break;
                case "ENCRYPTION_RESPONSE_RECEIVED":
                    initialLoginSessionHandler$loginStateEnum$ENCRYPTION_RESPONSE_RECEIVED = enumObject;
                    break;
            }
        }
        Objects.requireNonNull(initialLoginSessionHandler$loginStateEnum$LOGIN_PACKET_EXPECTED, "LOGIN_PACKET_EXPECTED");
        Objects.requireNonNull(initialLoginSessionHandler$loginStateEnum$LOGIN_PACKET_RECEIVED, "LOGIN_PACKET_RECEIVED");
        Objects.requireNonNull(initialLoginSessionHandler$loginStateEnum$ENCRYPTION_REQUEST_SENT, "ENCRYPTION_REQUEST_SENT");
        Objects.requireNonNull(initialLoginSessionHandler$loginStateEnum$ENCRYPTION_RESPONSE_RECEIVED, "ENCRYPTION_RESPONSE_RECEIVED");

        MethodHandles.Lookup lookup = MethodHandles.lookup();
        initialLoginSessionHandler_assertStateMethod = lookup.unreflect(ReflectUtil.handleAccessible(
                initialLoginSessionHandlerClass.getDeclaredMethod("assertState", initialLoginSessionHandler$loginStateEnum)
        ));

        initialLoginSessionHandler_setCurrentStateField = lookup.unreflectSetter(ReflectUtil.handleAccessible(
                initialLoginSessionHandlerClass.getDeclaredField("currentState")
        ));

        initialLoginSessionHandler_getLoginField = lookup.unreflectGetter(ReflectUtil.handleAccessible(
                initialLoginSessionHandlerClass.getDeclaredField("login")
        ));

        initialLoginSessionHandler_getVerifyField = lookup.unreflectGetter(ReflectUtil.handleAccessible(
                initialLoginSessionHandlerClass.getDeclaredField("verify")
        ));

        initialLoginSessionHandler_getServerField = lookup.unreflectGetter(ReflectUtil.handleAccessible(
                initialLoginSessionHandlerClass.getDeclaredField("server")
        ));

        initialLoginSessionHandler_getInboundField = lookup.unreflectGetter(ReflectUtil.handleAccessible(
                initialLoginSessionHandlerClass.getDeclaredField("inbound")
        ));

        initialLoginSessionHandler_getMcConnectionField = lookup.unreflectGetter(ReflectUtil.handleAccessible(
                initialLoginSessionHandlerClass.getDeclaredField("mcConnection")
        ));

        initialLoginSessionHandler_getCurrentStateField = lookup.unreflectGetter(ReflectUtil.handleAccessible(
                initialLoginSessionHandlerClass.getDeclaredField("currentState")
        ));

        authSessionHandler_allArgsConstructor = lookup.unreflectConstructor(ReflectUtil.handleAccessible(
                AuthSessionHandler.class.getDeclaredConstructor(
                        VelocityServer.class,
                        LoginInboundConnection.class,
                        GameProfile.class,
                        boolean.class
                )
        ));
    }

    private void initValues() throws Throwable {
        this.login = (ServerLogin) initialLoginSessionHandler_getLoginField.invoke(initialLoginSessionHandler);
        this.verify = (byte[]) initialLoginSessionHandler_getVerifyField.invoke(initialLoginSessionHandler);
    }

    public void handle(EncryptionResponse packet) throws Throwable {
        initValues();

        // 模拟常规流程
        initialLoginSessionHandler_assertStateMethod.invoke(initialLoginSessionHandler, initialLoginSessionHandler$loginStateEnum$ENCRYPTION_REQUEST_SENT);
        initialLoginSessionHandler_setCurrentStateField.invoke(initialLoginSessionHandler, initialLoginSessionHandler$loginStateEnum$ENCRYPTION_RESPONSE_RECEIVED);

        ServerLogin login = this.login;
        if (login == null) {
            throw new IllegalStateException("No ServerLogin packet received yet.");
        }
        if (this.verify.length == 0) {
            throw new IllegalStateException("No EncryptionRequest packet sent yet.");
        }

        try {

            // 加密部分
            KeyPair serverKeyPair = this.server.getServerKeyPair();
            if (this.inbound.getIdentifiedKey() != null) {
                IdentifiedKey playerKey = this.inbound.getIdentifiedKey();
                if (!playerKey.verifyDataSignature(packet.getVerifyToken(), this.verify, Longs.toByteArray(packet.getSalt()))) {
                    throw new IllegalStateException("Invalid client public signature.");
                }
            } else {
                byte[] decryptedSharedSecret = EncryptionUtils.decryptRsa(serverKeyPair, packet.getVerifyToken());
                if (!MessageDigest.isEqual(this.verify, decryptedSharedSecret)) {
                    throw new IllegalStateException("Unable to successfully decrypt the verification token.");
                }
            }

            byte[] decryptedSharedSecret = EncryptionUtils.decryptRsa(serverKeyPair, packet.getSharedSecret());

            encrypted = true;
            // 验证
            String username = login.getUsername();
            String serverId = EncryptionUtils.generateServerId(decryptedSharedSecret, serverKeyPair.getPublic());
            String playerIp = ((InetSocketAddress) this.mcConnection.getRemoteAddress()).getHostString();

            AuthResult result = multiCoreAPI.getAuthHandler().auth(username, serverId, playerIp);

            if (this.mcConnection.isClosed()) return;
            try {
                this.mcConnection.enableEncryption(decryptedSharedSecret);
            } catch (GeneralSecurityException var8) {
                LoggerProvider.getLogger().error("Unable to enable encryption for connection", var8);
                this.mcConnection.close(true);
                return;
            }
            if (result.isAllowed()) {
                this.mcConnection.setSessionHandler(
                        (AuthSessionHandler) authSessionHandler_allArgsConstructor.invoke(
                                this.server, inbound, generateGameProfile(result.getResponse()), true
                        )
                );
            } else {
                this.inbound.disconnect(Component.text(result.getKickMessage()));
            }
        } catch (GeneralSecurityException var9) {
            LoggerProvider.getLogger().error("Unable to enable encryption.", var9);
            this.mcConnection.close(true);
        }
    }

    private GameProfile generateGameProfile(HasJoinedResponse response) {
        return new GameProfile(
                response.getId(),
                response.getName(),
                response.getPropertyMap().values().stream().map(s ->
                        new GameProfile.Property(s.getName(), s.getValue(), s.getSignature())
                ).collect(Collectors.toList())
        );
    }
}
