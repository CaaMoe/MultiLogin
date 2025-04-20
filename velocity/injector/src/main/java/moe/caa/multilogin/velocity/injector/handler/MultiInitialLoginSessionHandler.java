package moe.caa.multilogin.velocity.injector.handler;

import com.google.common.primitives.Longs;
import com.velocitypowered.api.proxy.crypto.IdentifiedKey;
import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.connection.MinecraftConnection;
import com.velocitypowered.proxy.connection.client.AuthSessionHandler;
import com.velocitypowered.proxy.connection.client.InitialLoginSessionHandler;
import com.velocitypowered.proxy.connection.client.LoginInboundConnection;
import com.velocitypowered.proxy.crypto.EncryptionUtils;
import com.velocitypowered.proxy.protocol.StateRegistry;
import com.velocitypowered.proxy.protocol.packet.EncryptionResponsePacket;
import com.velocitypowered.proxy.protocol.packet.ServerLoginPacket;
import lombok.Getter;
import moe.caa.multilogin.api.internal.auth.AuthResult;
import moe.caa.multilogin.api.profile.GameProfile;
import moe.caa.multilogin.api.internal.logger.LoggerProvider;
import moe.caa.multilogin.api.internal.main.MultiCoreAPI;
import moe.caa.multilogin.api.internal.skinrestorer.SkinRestorerResult;
import moe.caa.multilogin.api.internal.util.reflect.Accessor;
import moe.caa.multilogin.api.internal.util.reflect.EnumAccessor;
import moe.caa.multilogin.api.internal.util.reflect.NoSuchEnumException;
import moe.caa.multilogin.api.internal.util.reflect.ReflectUtil;
import moe.caa.multilogin.core.auth.LoginAuthResult;
import net.kyori.adventure.text.Component;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.util.stream.Collectors;

/**
 * 接管 InitialLoginSessionHandler 类的其中一个方法
 */
@Getter()
public class MultiInitialLoginSessionHandler {
    private static EnumAccessor loginStatsEnumAccessor;
    private static Accessor initialLoginSessionHandlerAccessor;

    // LoginStateEnum 的枚举
    private static Enum<?> loginStateEnum$LOGIN_PACKET_EXPECTED;
    private static Enum<?> loginStateEnum$LOGIN_PACKET_RECEIVED;
    private static Enum<?> loginStateEnum$ENCRYPTION_REQUEST_SENT;
    private static Enum<?> loginStateEnum$ENCRYPTION_RESPONSE_RECEIVED;

    // 一些函数和字段的引用
    private static MethodHandle assertStateMethod;
    private static MethodHandle setCurrentStateField;
    private static MethodHandle getLoginField;
    private static MethodHandle getVerifyField;
    private static MethodHandle getServerField;
    private static MethodHandle getInboundField;
    private static MethodHandle getMcConnectionField;
    private static MethodHandle getCurrentStateField;
    private static MethodHandle authSessionHandler_allArgsConstructor;
    // 类体常量
    private final InitialLoginSessionHandler initialLoginSessionHandler;
    private final MultiCoreAPI multiCoreAPI; // 这个不是
    private final VelocityServer server;
    private final MinecraftConnection mcConnection;
    private final LoginInboundConnection inbound;
    // 运行时改动的实例
    private ServerLoginPacket login;
    private byte[] verify;
    // 自己的对象，表示是否通过加密
    private boolean encrypted = false;

    public MultiInitialLoginSessionHandler(InitialLoginSessionHandler initialLoginSessionHandler, MultiCoreAPI multiCoreAPI) {
        this.initialLoginSessionHandler = initialLoginSessionHandler;
        this.multiCoreAPI = multiCoreAPI;
        try {
            this.server = (VelocityServer) getServerField.invoke(initialLoginSessionHandler);
            this.mcConnection = (MinecraftConnection) getMcConnectionField.invoke(initialLoginSessionHandler);
            this.inbound = (LoginInboundConnection) getInboundField.invoke(initialLoginSessionHandler);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static void init() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, NoSuchFieldException, NoSuchEnumException {
        Class<InitialLoginSessionHandler> initialLoginSessionHandlerClass = InitialLoginSessionHandler.class;
        initialLoginSessionHandlerAccessor = new Accessor(initialLoginSessionHandlerClass);

        Class<?> loginStateEnum = Class.forName("com.velocitypowered.proxy.connection.client.InitialLoginSessionHandler$LoginState");
        loginStatsEnumAccessor = new EnumAccessor(loginStateEnum);

        loginStateEnum$LOGIN_PACKET_EXPECTED = loginStatsEnumAccessor.findByName("LOGIN_PACKET_EXPECTED");
        loginStateEnum$LOGIN_PACKET_RECEIVED = loginStatsEnumAccessor.findByName("LOGIN_PACKET_RECEIVED");
        loginStateEnum$ENCRYPTION_REQUEST_SENT = loginStatsEnumAccessor.findByName("ENCRYPTION_REQUEST_SENT");
        loginStateEnum$ENCRYPTION_RESPONSE_RECEIVED = loginStatsEnumAccessor.findByName("ENCRYPTION_RESPONSE_RECEIVED");

        MethodHandles.Lookup lookup = MethodHandles.lookup();


        assertStateMethod = lookup.unreflect(ReflectUtil.handleAccessible(
                initialLoginSessionHandlerAccessor.findFirstMethodByName(true, "assertState")
        ));

        Field currentState = ReflectUtil.handleAccessible(
                initialLoginSessionHandlerClass.getDeclaredField("currentState")
        );
        getCurrentStateField = lookup.unreflectGetter(currentState);
        setCurrentStateField = lookup.unreflectSetter(currentState);

        getLoginField = lookup.unreflectGetter(ReflectUtil.handleAccessible(
                initialLoginSessionHandlerAccessor.findFirstFieldByType(true, ServerLoginPacket.class)
        ));

        getVerifyField = lookup.unreflectGetter(ReflectUtil.handleAccessible(
                initialLoginSessionHandlerAccessor.findFirstFieldByType(true, byte[].class)
        ));

        getServerField = lookup.unreflectGetter(ReflectUtil.handleAccessible(
                initialLoginSessionHandlerAccessor.findFirstFieldByType(true, VelocityServer.class)
        ));

        getInboundField = lookup.unreflectGetter(ReflectUtil.handleAccessible(
                initialLoginSessionHandlerAccessor.findFirstFieldByType(true, LoginInboundConnection.class)
        ));

        getMcConnectionField = lookup.unreflectGetter(ReflectUtil.handleAccessible(
                initialLoginSessionHandlerAccessor.findFirstFieldByType(true, MinecraftConnection.class)
        ));


        authSessionHandler_allArgsConstructor = lookup.unreflectConstructor(ReflectUtil.handleAccessible(
                AuthSessionHandler.class.getDeclaredConstructor(
                        VelocityServer.class,
                        LoginInboundConnection.class,
                        com.velocitypowered.api.util.GameProfile.class,
                        boolean.class
                )
        ));
    }

    private void initValues() throws Throwable {
        this.login = (ServerLoginPacket) getLoginField.invoke(initialLoginSessionHandler);
        this.verify = (byte[]) getVerifyField.invoke(initialLoginSessionHandler);
    }

    public void handle(EncryptionResponsePacket packet) throws Throwable {
        initValues();

        // 模拟常规流程
        assertStateMethod.invoke(initialLoginSessionHandler, loginStateEnum$ENCRYPTION_REQUEST_SENT);
        setCurrentStateField.invoke(initialLoginSessionHandler, loginStateEnum$ENCRYPTION_RESPONSE_RECEIVED);

        ServerLoginPacket login = this.login;
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

            multiCoreAPI.getPlugin().getRunServer().getScheduler().runTaskAsync(() -> {
                LoginAuthResult result = (LoginAuthResult) multiCoreAPI.getAuthHandler().auth(username, serverId, playerIp);
                try {
                    if (mcConnection.getChannel().eventLoop().submit(() -> {
                        if (this.mcConnection.isClosed()) return false;
                        try {
                            this.mcConnection.enableEncryption(decryptedSharedSecret);
                            return true;
                        } catch (GeneralSecurityException var8) {
                            LoggerProvider.getLogger().error("Unable to enable encryption for connection", var8);
                            this.mcConnection.close(true);
                            return false;
                        }
                    }).get()) {
                        if (result.getResult() == AuthResult.Result.ALLOW) {
                            GameProfile gameProfile = result.getResponse();

                            try {
                                SkinRestorerResult restorerResult = multiCoreAPI.getSkinRestorerHandler().doRestorer(result);
                                if (restorerResult.getThrowable() != null) {
                                    LoggerProvider.getLogger().error("An exception occurred while processing the skin repair.", restorerResult.getThrowable());
                                }
                                LoggerProvider.getLogger().debug(String.format("Skin restore result of %s is %s.", result.getBaseServiceAuthenticationResult().getResponse().getName(), restorerResult.getReason()));

                                if (restorerResult.getResponse() != null) {
                                    gameProfile = restorerResult.getResponse();
                                }
                            } catch (Exception e) {
                                LoggerProvider.getLogger().debug(String.format("Skin restore result of %s is %s.", result.getBaseServiceAuthenticationResult().getResponse().getName(), "error"));
                                LoggerProvider.getLogger().debug("An exception occurred while processing the skin repair.", e);
                            }

                            GameProfile finalGameProfile = gameProfile;
                            mcConnection.getChannel().eventLoop().submit(() -> {
                                try {
                                    this.mcConnection.setActiveSessionHandler(StateRegistry.LOGIN,
                                            (AuthSessionHandler) authSessionHandler_allArgsConstructor.invoke(
                                                    this.server, inbound, generateGameProfile(finalGameProfile), true
                                            ));
                                } catch (Throwable e) {
                                    throw new RuntimeException(e);
                                }
                            }).get();

                        } else {
                            this.inbound.disconnect(Component.text(result.getKickMessage()));
                        }
                    }
                } catch (Throwable e){
                    LoggerProvider.getLogger().error("An exception occurred while processing validation results.", e);
                    if (isEncrypted()) {
                        getInbound().disconnect(Component.text(multiCoreAPI.getLanguageHandler().getMessage("auth_error")));
                    }
                    mcConnection.close(true);
                }
            });
        } catch (GeneralSecurityException var9) {
            LoggerProvider.getLogger().error("Unable to enable encryption.", var9);
            this.mcConnection.close(true);
        }
    }

    private com.velocitypowered.api.util.GameProfile generateGameProfile(GameProfile response) {
        return new com.velocitypowered.api.util.GameProfile(
                response.getId(),
                response.getName(),
                response.getPropertyMap().values().stream().map(s ->
                        new com.velocitypowered.api.util.GameProfile.Property(s.getName(), s.getValue(), s.getSignature())
                ).collect(Collectors.toList())
        );
    }
}
