package moe.caa.multilogin.bukkit.injector.nms.v1_19_r1.handler;

import com.mojang.authlib.GameProfile;
import lombok.Getter;
import moe.caa.multilogin.api.auth.AuthResult;
import moe.caa.multilogin.api.auth.Property;
import moe.caa.multilogin.api.logger.LoggerProvider;
import moe.caa.multilogin.api.main.MultiCoreAPI;
import moe.caa.multilogin.api.util.ReflectUtil;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.protocol.login.PacketLoginInEncryptionBegin;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.LoginListener;
import net.minecraft.util.CryptographyException;
import net.minecraft.util.MinecraftEncryption;
import net.minecraft.world.entity.player.ProfilePublicKey;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nullable;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.PrivateKey;
import java.util.Map;
import java.util.Objects;

/**
 * 接管 LoginListener 的其中一个方法
 */
public class MultiPacketLoginInEncryptionBeginHandler {
    // EnumProtocolState 的枚举
    private static Enum<?> enumProtocolState$HELLO;
    private static Enum<?> enumProtocolState$KEY;
    private static Enum<?> enumProtocolState$AUTHENTICATING;
    private static Enum<?> enumProtocolState$NEGOTIATING;
    private static Enum<?> enumProtocolState$READY_TO_ACCEPT;
    private static Enum<?> enumProtocolState$DELAY_ACCEPT;
    private static Enum<?> enumProtocolState$ACCEPTED;

    @Getter
    private final LoginListener loginListener;
    private final MultiCoreAPI multiCoreAPI;


    private static MethodHandle stateFieldGetter;
    private static MethodHandle stateFieldSetter;
    private static MethodHandle serverFieldGetter;
    private static MethodHandle profilePublicKeyDataFieldGetter;
    private static MethodHandle nonceFieldGetter;
    private static MethodHandle connectionFieldGetter;
    private static MethodHandle gameProfileFieldGetter;
    private static MethodHandle gameProfileFieldSetter;

    private Enum<?> state;
    private MinecraftServer server;
    private ProfilePublicKey.a profilePublicKeyData;
    private byte[] nonce;
    public NetworkManager connection;
    public GameProfile gameProfile;


    public MultiPacketLoginInEncryptionBeginHandler(LoginListener loginListener, MultiCoreAPI multiCoreAPI) {
        this.loginListener = loginListener;
        this.multiCoreAPI = multiCoreAPI;
    }

    public static void init() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        Class<?> stateEnum = Class.forName("net.minecraft.server.network.LoginListener$EnumProtocolState");

        for (int i = 0; i < stateEnum.getEnumConstants().length; i++) {
            switch (i) {
                case 0 -> enumProtocolState$HELLO = (Enum<?>) stateEnum.getEnumConstants()[i];
                case 1 -> enumProtocolState$KEY = (Enum<?>) stateEnum.getEnumConstants()[i];
                case 2 -> enumProtocolState$AUTHENTICATING = (Enum<?>) stateEnum.getEnumConstants()[i];
                case 3 -> enumProtocolState$NEGOTIATING = (Enum<?>) stateEnum.getEnumConstants()[i];
                case 4 -> enumProtocolState$READY_TO_ACCEPT = (Enum<?>) stateEnum.getEnumConstants()[i];
                case 5 -> enumProtocolState$DELAY_ACCEPT = (Enum<?>) stateEnum.getEnumConstants()[i];
                case 6 -> enumProtocolState$ACCEPTED = (Enum<?>) stateEnum.getEnumConstants()[i];
            }
        }

        MethodHandles.Lookup lookup = MethodHandles.lookup();
        stateFieldGetter = lookup.unreflectGetter(ReflectUtil.handleAccessible(
                LoginListener.class.getDeclaredField("h")
        ));

        stateFieldSetter = lookup.unreflectSetter(ReflectUtil.handleAccessible(
                LoginListener.class.getDeclaredField("h")
        ));

        serverFieldGetter = lookup.unreflectSetter(ReflectUtil.handleAccessible(
                LoginListener.class.getDeclaredField("g")
        ));
        profilePublicKeyDataFieldGetter = lookup.unreflectSetter(ReflectUtil.handleAccessible(
                LoginListener.class.getDeclaredField("m")
        ));
        nonceFieldGetter = lookup.unreflectGetter(ReflectUtil.handleAccessible(
                LoginListener.class.getDeclaredField("f")
        ));
        connectionFieldGetter = lookup.unreflectGetter(ReflectUtil.handleAccessible(
                LoginListener.class.getDeclaredField("a")
        ));

        gameProfileFieldGetter = lookup.unreflectGetter(ReflectUtil.handleAccessible(
                LoginListener.class.getDeclaredField("j")
        ));
        gameProfileFieldSetter = lookup.unreflectSetter(ReflectUtil.handleAccessible(
                LoginListener.class.getDeclaredField("j")
        ));
    }

    private void initValues() throws Throwable {
        this.state = (Enum<?>) stateFieldGetter.invoke(loginListener);
        this.server = (MinecraftServer) serverFieldGetter.invoke(loginListener);
        this.profilePublicKeyData = (ProfilePublicKey.a) profilePublicKeyDataFieldGetter.invoke(loginListener);
        this.nonce = (byte[]) nonceFieldGetter.invoke(loginListener);
        this.connection = (NetworkManager) connectionFieldGetter.invoke(loginListener);
        this.gameProfile = (GameProfile) gameProfileFieldGetter.invoke(loginListener);
    }

    public void handle(PacketLoginInEncryptionBegin packetLoginInEncryptionBegin) throws Throwable {
        initValues();

        Validate.validState(this.state == enumProtocolState$KEY, "Unexpected key packet");

        String serverId;

        try {
            PrivateKey privatekey = this.server.K().getPrivate();

            if (this.profilePublicKeyData != null) {
                ProfilePublicKey profilepublickey = ProfilePublicKey.a(this.profilePublicKeyData);

                if (!packetLoginInEncryptionBegin.a(this.nonce, profilepublickey)) {
                    throw new IllegalStateException("Protocol error");
                }
            } else if (!packetLoginInEncryptionBegin.a(this.nonce, privatekey)) {
                throw new IllegalStateException("Protocol error");
            }

            SecretKey secretkey = packetLoginInEncryptionBegin.a(privatekey);
            Cipher cipher = MinecraftEncryption.a(2, secretkey);
            Cipher cipher1 = MinecraftEncryption.a(1, secretkey);


            serverId = (new BigInteger(MinecraftEncryption.a("", this.server.K().getPublic(), secretkey))).toString(16);
            stateFieldSetter.invoke(loginListener, enumProtocolState$AUTHENTICATING);
            this.connection.a(cipher, cipher1);
        } catch (CryptographyException cryptographyexception) {
            throw new IllegalStateException("Protocol error", cryptographyexception);
        }

        multiCoreAPI.getPlugin().getRunServer().getScheduler().runTaskAsync(()->{
            InetAddress address = getAddress();
            AuthResult authResult = multiCoreAPI.getAuthHandler().auth(gameProfile.getName(), serverId, address == null ? null : address.getHostAddress());
            if (!connection.h()) {
                return;
            }
            try {
                if (authResult.isAllowed()) {
                    GameProfile profile = generateGameProfile(authResult.getResponse());
                    gameProfileFieldSetter.invoke(loginListener, profile);
                    fireEvent();
                } else {
                    loginListener.disconnect(authResult.getKickMessage());
                }
            } catch (Throwable e) {
                loginListener.disconnect(multiCoreAPI.getLanguageHandler().getMessage("bukkit_auth_error"));
                LoggerProvider.getLogger().error("An exception occurred while processing a login request.", e);
            }

        });
    }


    @Nullable
    private InetAddress getAddress() {
        SocketAddress socketaddress = connection.n;
        return socketaddress instanceof InetSocketAddress ? ((InetSocketAddress) socketaddress).getAddress() : null;
    }

    private GameProfile generateGameProfile(moe.caa.multilogin.api.auth.GameProfile response) {
        GameProfile profile = new GameProfile(
                response.getId(),
                response.getName()
        );
        for (Map.Entry<String, Property> entry : response.getPropertyMap().entrySet()) {
            profile.getProperties().put(entry.getKey(), new com.mojang.authlib.properties.Property(
                    entry.getValue().getName(),
                    entry.getValue().getValue(),
                    entry.getValue().getSignature()
            ));
        }
        return profile;
    }

    private void fireEvent() throws Exception {
        Class<LoginListener.LoginHandler> handlerClass = LoginListener.LoginHandler.class;
        LoginListener.LoginHandler handler = handlerClass.getConstructor().newInstance();
        handler.fireEvents();
    }
}
