package fun.ksnb.multilogin.velocity.pccsh;

import com.google.common.primitives.Longs;
import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.connection.MinecraftConnection;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.connection.client.InitialLoginSessionHandler;
import com.velocitypowered.proxy.connection.client.LoginInboundConnection;
import com.velocitypowered.proxy.crypto.EncryptionUtils;
import com.velocitypowered.proxy.protocol.packet.EncryptionResponse;
import com.velocitypowered.proxy.protocol.packet.ServerLogin;
import fun.ksnb.multilogin.velocity.auth.Disconnectable;
import fun.ksnb.multilogin.velocity.main.MultiLoginVelocityPluginBootstrap;
import moe.caa.multilogin.core.impl.BaseUserLogin;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.logger.MultiLogger;
import moe.caa.multilogin.core.util.ReflectUtil;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.net.InetSocketAddress;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.MessageDigest;

import static com.velocitypowered.proxy.connection.VelocityConstants.EMPTY_BYTE_ARRAY;

public class MultiLoginEncryptionResponse extends EncryptionResponse {

    private static MethodHandle LOGIN_SESSION_HANDLER_SERVER_LOGIN_FIELD;
    private static MethodHandle LOGIN_SESSION_HANDLER_SERVER_VERIFY_FIELD;
    private static MethodHandle LOGIN_SESSION_HANDLER_SERVER_MC_CONNECTION_FIELD;
    private static MethodHandle LOGIN_SESSION_HANDLER_SERVER_INBOUND_FIELD;

    private final VelocityServer server;

    //    原有
    private ServerLogin login = null;
    private byte[] verify = EMPTY_BYTE_ARRAY;
    private InitialLoginSessionHandler loginSessionHandler;
    private LoginInboundConnection inbound;
    //    运行中产生
    private byte[] decryptedSharedSecret = EMPTY_BYTE_ARRAY;
    private KeyPair serverKeyPair;
    private MinecraftConnection mcConnection;
    private Disconnectable disconnectable;

    public MultiLoginEncryptionResponse() {
        server = (VelocityServer) MultiLoginVelocityPluginBootstrap.getInstance().getServer();
    }

    public static void init() throws IllegalAccessException, NoSuchFieldException {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        LOGIN_SESSION_HANDLER_SERVER_LOGIN_FIELD = lookup.unreflectGetter(ReflectUtil.handleAccessible(InitialLoginSessionHandler.class.getDeclaredField("login"), true));
        LOGIN_SESSION_HANDLER_SERVER_VERIFY_FIELD = lookup.unreflectGetter(ReflectUtil.handleAccessible(InitialLoginSessionHandler.class.getDeclaredField("verify"), true));
        LOGIN_SESSION_HANDLER_SERVER_MC_CONNECTION_FIELD = lookup.unreflectGetter(ReflectUtil.handleAccessible(InitialLoginSessionHandler.class.getDeclaredField("mcConnection"), true));
        LOGIN_SESSION_HANDLER_SERVER_INBOUND_FIELD = lookup.unreflectGetter(ReflectUtil.handleAccessible(InitialLoginSessionHandler.class.getDeclaredField("inbound"), true));
    }

    /**
     * @param handler velocity内的处理
     * @return 能否处理该包 必须是true 否则会被其他模块再次处理
     */
    @Override
    public boolean handle(MinecraftSessionHandler handler) {
        // 不合法
        if (!(handler instanceof InitialLoginSessionHandler)) return true;
        loginSessionHandler = (InitialLoginSessionHandler) handler;
        //初始化全部数值
        getValues();

        // 模拟常规流程
        if (login == null) {
            throw new IllegalStateException("No ServerLogin packet received yet.");
        }
        if (verify.length == 0) {
            throw new IllegalStateException("No EncryptionRequest packet sent yet.");
        }
        //velocity正常的加密配置方法
        if (!enableEncrypt()) return true;

        String username = login.getUsername();
        String serverId = EncryptionUtils.generateServerId(decryptedSharedSecret, serverKeyPair.getPublic());
        String playerIp = ((InetSocketAddress) mcConnection.getRemoteAddress()).getHostString();

        BaseUserLogin userLogin = new VelocityUserLogin(username, serverId, playerIp, loginSessionHandler, disconnectable);
        MultiLoginVelocityPluginBootstrap.getInstance().getCore().getAuthCore().doAuth(userLogin);

        return true;
    }


    private boolean enableEncrypt() {
        try {
            serverKeyPair = server.getServerKeyPair();
            if (this.inbound.getIdentifiedKey() != null) {
                if (!this.inbound.getIdentifiedKey().verifyDataSignature(getVerifyToken(), this.verify, Longs.toByteArray(getSalt()))) {
                    throw new IllegalStateException("Invalid client public signature.");
                }
            } else {
                decryptedSharedSecret = EncryptionUtils.decryptRsa(serverKeyPair, getVerifyToken());
                if (!MessageDigest.isEqual(this.verify, decryptedSharedSecret)) {
                    throw new IllegalStateException("Unable to successfully decrypt the verification token.");
                }
            }
            decryptedSharedSecret = EncryptionUtils.decryptRsa(serverKeyPair, getSharedSecret());

            try {
                mcConnection.enableEncryption(decryptedSharedSecret);
            } catch (GeneralSecurityException e) {
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            MultiLogger.getLogger().log(LoggerLevel.ERROR, "Unable to enable encryption.", e);
            mcConnection.close(true);
            return false;
        }
        return true;
    }

    private void getValues() {
        try {
            login = (ServerLogin) LOGIN_SESSION_HANDLER_SERVER_LOGIN_FIELD.invoke(loginSessionHandler);
            verify = (byte[]) LOGIN_SESSION_HANDLER_SERVER_VERIFY_FIELD.invoke(loginSessionHandler);
            mcConnection = (MinecraftConnection) LOGIN_SESSION_HANDLER_SERVER_MC_CONNECTION_FIELD.invoke(loginSessionHandler);
            disconnectable = Disconnectable.generateDisconnectable(inbound = (LoginInboundConnection) LOGIN_SESSION_HANDLER_SERVER_INBOUND_FIELD.invoke(loginSessionHandler));
        } catch (Throwable t) {
            MultiLogger.getLogger().log(LoggerLevel.ERROR, "Exception during assignment.", t);
        }
    }
}
