package fun.ksnb.multilogin.velocity.auth;

import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.connection.MinecraftConnection;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.connection.client.InitialInboundConnection;
import com.velocitypowered.proxy.connection.client.LoginSessionHandler;
import com.velocitypowered.proxy.protocol.packet.EncryptionResponse;
import com.velocitypowered.proxy.protocol.packet.ServerLogin;
import fun.ksnb.multilogin.velocity.impl.VelocityUserLogin;
import fun.ksnb.multilogin.velocity.main.MultiLoginVelocity;
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
import static com.velocitypowered.proxy.util.EncryptionUtils.decryptRsa;
import static com.velocitypowered.proxy.util.EncryptionUtils.generateServerId;

public class MultiLoginEncryptionResponse extends EncryptionResponse {

    private static MethodHandle LOGIN_SESSION_HANDLER_SERVER_LOGIN_FIELD;
    private static MethodHandle LOGIN_SESSION_HANDLER_SERVER_VERIFY_FIELD;
    private static MethodHandle LOGIN_SESSION_HANDLER_SERVER_MC_CONNECTION_FIELD;
    private static MethodHandle LOGIN_SESSION_HANDLER_SERVER_INBOUND_FIELD;

    private final VelocityServer server;

    //    原有
    private ServerLogin login = null;
    private byte[] verify = EMPTY_BYTE_ARRAY;
    private LoginSessionHandler loginSessionHandler;
    //    运行种产生
    private byte[] decryptedSharedSecret = EMPTY_BYTE_ARRAY;
    private KeyPair serverKeyPair;
    private MinecraftConnection mcConnection;
    private InitialInboundConnection inbound;

    public MultiLoginEncryptionResponse() {
        server = (VelocityServer) MultiLoginVelocity.getInstance().getServer();
    }

    public static void init() throws IllegalAccessException, NoSuchFieldException {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        LOGIN_SESSION_HANDLER_SERVER_LOGIN_FIELD = lookup.unreflectGetter(ReflectUtil.handleAccessible(LoginSessionHandler.class.getDeclaredField("login"), true));
        LOGIN_SESSION_HANDLER_SERVER_VERIFY_FIELD = lookup.unreflectGetter(ReflectUtil.handleAccessible(LoginSessionHandler.class.getDeclaredField("verify"), true));
        LOGIN_SESSION_HANDLER_SERVER_MC_CONNECTION_FIELD = lookup.unreflectGetter(ReflectUtil.handleAccessible(LoginSessionHandler.class.getDeclaredField("mcConnection"), true));
        LOGIN_SESSION_HANDLER_SERVER_INBOUND_FIELD = lookup.unreflectGetter(ReflectUtil.handleAccessible(LoginSessionHandler.class.getDeclaredField("inbound"), true));
    }

    @Override
    public boolean handle(MinecraftSessionHandler handler) {
        // 不合法
        if (!(handler instanceof LoginSessionHandler)) return false;
        loginSessionHandler = (LoginSessionHandler) handler;
        getValues();

        // 模拟常规流程
        if (login == null) {
            throw new IllegalStateException("No ServerLogin packet received yet.");
        }
        if (verify.length == 0) {
            throw new IllegalStateException("No EncryptionRequest packet sent yet.");
        }
        if (!enableEncrypt()) return true;

        String username = login.getUsername();
        String serverId = generateServerId(decryptedSharedSecret, serverKeyPair.getPublic());
        String playerIp = ((InetSocketAddress) mcConnection.getRemoteAddress()).getHostString();

        BaseUserLogin userLogin = new VelocityUserLogin(username, serverId, playerIp, loginSessionHandler, inbound);
        MultiLoginVelocity.getInstance().getRunServer().getScheduler().runTaskAsync(() -> {
            MultiLoginVelocity.getInstance().getCore().getAuthCore().doAuth(userLogin);
        });

        return true;
    }


    private boolean enableEncrypt() {
        try {
            serverKeyPair = server.getServerKeyPair();
            byte[] decryptedVerifyToken = decryptRsa(serverKeyPair, getVerifyToken());
            if (!MessageDigest.isEqual(verify, decryptedVerifyToken)) {
                throw new IllegalStateException("无法成功解密验证令牌。");
            }
            if (mcConnection.isClosed()) return false;
            decryptedSharedSecret = decryptRsa(serverKeyPair, getSharedSecret());
            try {
                mcConnection.enableEncryption(decryptedSharedSecret);
            } catch (GeneralSecurityException e) {
                throw new RuntimeException(e);
            }
        } catch (GeneralSecurityException e) {
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
            inbound = (InitialInboundConnection) LOGIN_SESSION_HANDLER_SERVER_INBOUND_FIELD.invoke(loginSessionHandler);
        } catch (Throwable t) {
            MultiLogger.getLogger().log(LoggerLevel.ERROR, "Exception during assignment.", t);
        }
    }
}
