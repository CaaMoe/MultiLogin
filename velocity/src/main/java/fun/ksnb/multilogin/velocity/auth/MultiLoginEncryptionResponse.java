package fun.ksnb.multilogin.velocity.auth;

import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.connection.MinecraftConnection;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.connection.client.InitialInboundConnection;
import com.velocitypowered.proxy.connection.client.LoginSessionHandler;
import com.velocitypowered.proxy.protocol.packet.EncryptionResponse;
import com.velocitypowered.proxy.protocol.packet.ServerLogin;
import com.velocitypowered.proxy.util.EncryptionUtils;
import fun.ksnb.multilogin.velocity.main.MultiLoginVelocity;
import lombok.SneakyThrows;
import moe.caa.multilogin.core.util.ReflectUtil;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.MessageDigest;

import static com.velocitypowered.proxy.connection.VelocityConstants.EMPTY_BYTE_ARRAY;

public class MultiLoginEncryptionResponse extends EncryptionResponse {

    private static MethodHandle LOGIN_SESSION_HANDLER_SERVER_LOGIN_FIELD;
    private static MethodHandle LOGIN_SESSION_HANDLER_SERVER_VERIFY_FIELD;
    private static MethodHandle LOGIN_SESSION_HANDLER_SERVER_MC_CONNECTION_FIELD;
    private static MethodHandle LOGIN_SESSION_HANDLER_SERVER_INBOUND_FIELD;

    public static void init() throws IllegalAccessException, NoSuchFieldException {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        LOGIN_SESSION_HANDLER_SERVER_LOGIN_FIELD = lookup.unreflectGetter(ReflectUtil.handleAccessible(LoginSessionHandler.class.getDeclaredField("login"), true));
        LOGIN_SESSION_HANDLER_SERVER_VERIFY_FIELD = lookup.unreflectGetter(ReflectUtil.handleAccessible(LoginSessionHandler.class.getDeclaredField("verify"), true));
        LOGIN_SESSION_HANDLER_SERVER_MC_CONNECTION_FIELD = lookup.unreflectGetter(ReflectUtil.handleAccessible(LoginSessionHandler.class.getDeclaredField("mcConnection"), true));
        LOGIN_SESSION_HANDLER_SERVER_INBOUND_FIELD = lookup.unreflectGetter(ReflectUtil.handleAccessible(LoginSessionHandler.class.getDeclaredField("inbound"), true));
    }

    @SneakyThrows
    @Override
    public boolean handle(MinecraftSessionHandler handler) {
        ServerLogin login = null;
        byte[] verify = EMPTY_BYTE_ARRAY;
        LoginSessionHandler loginSessionHandler;
        byte[] decryptedSharedSecret = EMPTY_BYTE_ARRAY;
        KeyPair serverKeyPair = ((VelocityServer) MultiLoginVelocity.getInstance().getServer()).getServerKeyPair();
        MinecraftConnection mcConnection;
        InitialInboundConnection inbound;

        // 不合法
        if (!(handler instanceof LoginSessionHandler)) return false;

        loginSessionHandler = (LoginSessionHandler) handler;
        login = (ServerLogin) LOGIN_SESSION_HANDLER_SERVER_LOGIN_FIELD.invoke(loginSessionHandler);
        verify = (byte[]) LOGIN_SESSION_HANDLER_SERVER_VERIFY_FIELD.invoke(loginSessionHandler);
        mcConnection = (MinecraftConnection) LOGIN_SESSION_HANDLER_SERVER_MC_CONNECTION_FIELD.invoke(loginSessionHandler);
        inbound = (InitialInboundConnection) LOGIN_SESSION_HANDLER_SERVER_INBOUND_FIELD.invoke(loginSessionHandler);

        // 模拟常规流程
        if(login == null){
            throw new IllegalStateException("No ServerLogin packet received yet.");
        }
        if (verify.length == 0) {
            throw new IllegalStateException("No EncryptionRequest packet sent yet.");
        }

        try {
            verify = addEncryption(serverKeyPair, verify, mcConnection);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            return false;
        }


        if (mcConnection.isClosed()) {
            // 玩家关了.
            return false;
        }

        //return super.handle(handler);
        return false;
    }


    private byte[] addEncryption(KeyPair serverKeyPair, byte[] verify, MinecraftConnection connection) throws GeneralSecurityException {
        byte[] decryptedVerifyToken = EncryptionUtils.decryptRsa(serverKeyPair, getVerifyToken());
        if (!MessageDigest.isEqual(verify, decryptedVerifyToken)) {
            throw new IllegalStateException("Unable to successfully decrypt the verification token.");
        }
        byte[] decryptedSharedSecret = EncryptionUtils.decryptRsa(serverKeyPair, getSharedSecret());
        connection.enableEncryption(decryptedSharedSecret);
        return decryptedSharedSecret;
    }
}
