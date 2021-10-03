package moe.caa.multilogin.bukkit.nms.v1_16_R3.proxy;

import lombok.SneakyThrows;
import moe.caa.multilogin.bukkit.nms.IEncryptionBeginProxy;
import moe.caa.multilogin.bukkit.nms.IncompatibleException;
import moe.caa.multilogin.bukkit.nms.v1_16_R3.impl.BukkitUserLogin;
import moe.caa.multilogin.core.auth.response.HasJoinedResponse;
import moe.caa.multilogin.core.exception.NoSuchEnumException;
import moe.caa.multilogin.core.util.ReflectUtil;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.Validate;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.PrivateKey;
import java.util.Arrays;
import java.util.UUID;

public class MultiPacketLoginInEncryptionBegin extends PacketLoginInEncryptionBegin implements IEncryptionBeginProxy {
    private static final Class<?> LOGIN_LISTENER_CLASS;
    private static final Class<? extends Enum<?>> LOGIN_LISTENER_ENUM_PROTOCOL_STATE_CLASS;

    private static final Field LOGIN_LISTENER_CURRENT_ENUM_PROTOCOL_STATE_FIELD;
    private static final Field LOGIN_LISTENER_MINECRAFT_SERVER_FIELD;
    private static final Field LOGIN_LISTENER_NONCE_BYTES_FIELD;
    private static final Field LOGIN_LISTENER_NETWORK_MANAGER_FIELD;
    private static final Field LOGIN_LISTENER_SECRET_KEY_FIELD;


    static {
        try {
            LOGIN_LISTENER_CLASS = LoginListener.class;
            LOGIN_LISTENER_ENUM_PROTOCOL_STATE_CLASS = (Class<? extends Enum<?>>) Class.forName("net.minecraft.server.v1_16_R3.LoginListener$EnumProtocolState");
            LOGIN_LISTENER_CURRENT_ENUM_PROTOCOL_STATE_FIELD = ReflectUtil.handleAccessible(LOGIN_LISTENER_CLASS.getDeclaredField("g"), true);
            LOGIN_LISTENER_MINECRAFT_SERVER_FIELD = ReflectUtil.handleAccessible(LOGIN_LISTENER_CLASS.getDeclaredField("server"), true);
            LOGIN_LISTENER_NONCE_BYTES_FIELD = ReflectUtil.handleAccessible(LOGIN_LISTENER_CLASS.getDeclaredField("e"), true);
            LOGIN_LISTENER_NETWORK_MANAGER_FIELD = ReflectUtil.handleAccessible(LOGIN_LISTENER_CLASS.getDeclaredField("networkManager"), true);
            LOGIN_LISTENER_SECRET_KEY_FIELD  = ReflectUtil.handleAccessible(LOGIN_LISTENER_CLASS.getDeclaredField("loginKey"), true);
        } catch (Exception e) {
            throw new IncompatibleException(e);
        }
    }

    @SneakyThrows
    public void a(PacketLoginInListener var0) {
        LoginListener listener = (LoginListener) var0;
        String serverId = startEncrypting(var0);
        String ip = getIp(var0);
        String username = listener.getGameProfile().getName();
        BukkitUserLogin userLogin = new BukkitUserLogin(listener, username, serverId, ip);
        HasJoinedResponse response = new HasJoinedResponse();
        response.setId(UUID.randomUUID());
        response.setName("WDNMD");
        userLogin.finish(response);
    }

    private String startEncrypting(PacketLoginInListener var0) throws IllegalAccessException, NoSuchEnumException {
        Validate.validState(
                LOGIN_LISTENER_CURRENT_ENUM_PROTOCOL_STATE_FIELD.get(var0) == ReflectUtil.getEnumIns(LOGIN_LISTENER_ENUM_PROTOCOL_STATE_CLASS, "KEY")
                , "Unexpected key packet"
        );
        MinecraftServer server = (MinecraftServer) LOGIN_LISTENER_MINECRAFT_SERVER_FIELD.get(var0);
        PrivateKey privatekey = server.getKeyPair().getPrivate();
        byte[] nonce = (byte[]) LOGIN_LISTENER_NONCE_BYTES_FIELD.get(var0);

        String serverId;
        try {
            if (!Arrays.equals(nonce, this.b(privatekey))) {
                throw new IllegalStateException("Protocol error");
            }
            SecretKey loginKey = this.a(privatekey);
            LOGIN_LISTENER_SECRET_KEY_FIELD.set(var0, loginKey);
            Cipher cipher = MinecraftEncryption.a(2, loginKey);
            Cipher cipher1 = MinecraftEncryption.a(1, loginKey);
            serverId = (new BigInteger(MinecraftEncryption.a("", server.getKeyPair().getPublic(), loginKey))).toString(16);
            LOGIN_LISTENER_CURRENT_ENUM_PROTOCOL_STATE_FIELD.set(var0
                    , ReflectUtil.getEnumIns(LOGIN_LISTENER_ENUM_PROTOCOL_STATE_CLASS, "AUTHENTICATING")
            );
            NetworkManager nm = (NetworkManager) LOGIN_LISTENER_NETWORK_MANAGER_FIELD.get(var0);
            nm.a(cipher, cipher1);
            return serverId;
        } catch (CryptographyException var6) {
            throw new IllegalStateException("Protocol error", var6);
        }
    }

    private String getIp(PacketLoginInListener var0) throws IllegalAccessException {
        NetworkManager nm = (NetworkManager) LOGIN_LISTENER_NETWORK_MANAGER_FIELD.get(var0);
        MinecraftServer server = (MinecraftServer) LOGIN_LISTENER_MINECRAFT_SERVER_FIELD.get(var0);
        SocketAddress socketaddress = nm.getSocketAddress();
        return server.W() && socketaddress instanceof InetSocketAddress ? ((InetSocketAddress) socketaddress).getAddress().getHostAddress() : null;
    }
}
