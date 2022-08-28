package moe.caa.multilogin.bukkit.injector.nms.v1_19_r1.handler;

import com.mojang.authlib.GameProfile;
import lombok.Getter;
import moe.caa.multilogin.api.auth.AuthResult;
import moe.caa.multilogin.api.auth.Property;
import moe.caa.multilogin.api.logger.LoggerProvider;
import moe.caa.multilogin.api.main.MultiCoreAPI;
import moe.caa.multilogin.api.util.reflect.ReflectUtil;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.protocol.login.PacketLoginInEncryptionBegin;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.LoginListener;
import net.minecraft.util.CryptographyException;
import net.minecraft.util.MinecraftEncryption;
import net.minecraft.world.entity.player.ProfilePublicKey;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nullable;
import javax.crypto.SecretKey;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.Key;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Map;

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
    private static MethodHandle minecraftServer$keyPairFieldGetter;


    private static MethodHandle minecraftEncryption$generateServerIdMethod;
    private static MethodHandle packetLoginInEncryptionBegin$verifySignedNonceMethod;
    private static MethodHandle packetLoginInEncryptionBegin$verifyEncryptedNonceMethod;
    private static MethodHandle packetLoginInEncryptionBegin$decryptSecretKeyMethod;
    private static MethodHandle minecraftEncryption$cipherFromKey;
    private static MethodHandle NetworkManager$setupEncryption;

    private static MethodHandle profilePublicKey$constructor;

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

    public static void init() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException {
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
                ReflectUtil.findNoStaticField(LoginListener.class, stateEnum)
        ));

        stateFieldSetter = lookup.unreflectSetter(ReflectUtil.handleAccessible(
                ReflectUtil.findNoStaticField(LoginListener.class, stateEnum)
        ));

        serverFieldGetter = lookup.unreflectGetter(ReflectUtil.handleAccessible(
                ReflectUtil.findNoStaticField(LoginListener.class, MinecraftServer.class)
        ));
        profilePublicKeyDataFieldGetter = lookup.unreflectGetter(ReflectUtil.handleAccessible(
                ReflectUtil.findNoStaticField(LoginListener.class, ProfilePublicKey.a.class)
        ));
        nonceFieldGetter = lookup.unreflectGetter(ReflectUtil.handleAccessible(
                ReflectUtil.findNoStaticField(LoginListener.class, byte[].class)
        ));
        connectionFieldGetter = lookup.unreflectGetter(ReflectUtil.handleAccessible(
                ReflectUtil.findNoStaticField(LoginListener.class, NetworkManager.class)
        ));

        gameProfileFieldGetter = lookup.unreflectGetter(ReflectUtil.handleAccessible(
                ReflectUtil.findNoStaticField(LoginListener.class, GameProfile.class)
        ));
        gameProfileFieldSetter = lookup.unreflectSetter(ReflectUtil.handleAccessible(
                ReflectUtil.findNoStaticField(LoginListener.class, GameProfile.class)
        ));

        minecraftServer$keyPairFieldGetter = lookup.unreflectGetter(ReflectUtil.handleAccessible(
                ReflectUtil.findNoStaticField(MinecraftServer.class, KeyPair.class)
        ));

        minecraftEncryption$generateServerIdMethod = lookup.unreflect(ReflectUtil.handleAccessible(
                ReflectUtil.findStaticMethodByParameters(
                        MinecraftEncryption.class,
                        String.class, PublicKey.class, SecretKey.class
                )
        ));
        minecraftEncryption$cipherFromKey = lookup.unreflect(ReflectUtil.handleAccessible(
                ReflectUtil.findStaticMethodByParameters(
                        MinecraftEncryption.class,
                        int.class, Key.class
                )
        ));

        profilePublicKey$constructor = lookup.unreflectConstructor(ReflectUtil.handleAccessible(
                ProfilePublicKey.class.getDeclaredConstructor(ProfilePublicKey.a.class)
        ));

        packetLoginInEncryptionBegin$verifySignedNonceMethod = lookup.unreflect(ReflectUtil.handleAccessible(
                ReflectUtil.findNoStaticMethodByParameters(
                        PacketLoginInEncryptionBegin.class,
                        byte[].class, ProfilePublicKey.class
                )
        ));
        packetLoginInEncryptionBegin$verifyEncryptedNonceMethod = lookup.unreflect(ReflectUtil.handleAccessible(
                ReflectUtil.findNoStaticMethodByParameters(
                        PacketLoginInEncryptionBegin.class,
                        byte[].class, PrivateKey.class
                )
        ));
        packetLoginInEncryptionBegin$decryptSecretKeyMethod = lookup.unreflect(ReflectUtil.handleAccessible(
                ReflectUtil.findNoStaticMethodByParameters(
                        PacketLoginInEncryptionBegin.class, PrivateKey.class
                )
        ));
        NetworkManager$setupEncryption = lookup.unreflect(ReflectUtil.handleAccessible(
                ReflectUtil.findNoStaticMethodByParameters(
                        NetworkManager.class,
                        SecretKey.class
                )
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
            KeyPair keyPair = (KeyPair) minecraftServer$keyPairFieldGetter.invoke(this.server);
            PrivateKey privatekey = keyPair.getPrivate();

            if (this.profilePublicKeyData != null) {
                ProfilePublicKey profilepublickey = (ProfilePublicKey) profilePublicKey$constructor.invoke(this.profilePublicKeyData);

                if (!((boolean) packetLoginInEncryptionBegin$verifySignedNonceMethod.invoke(packetLoginInEncryptionBegin, this.nonce, profilepublickey))) {
                    throw new IllegalStateException("Protocol error");
                }
            } else if (!((boolean) packetLoginInEncryptionBegin$verifyEncryptedNonceMethod.invoke(packetLoginInEncryptionBegin, this.nonce, privatekey))) {
                throw new IllegalStateException("Protocol error");
            }

            SecretKey secretkey = (SecretKey) packetLoginInEncryptionBegin$decryptSecretKeyMethod.invoke(packetLoginInEncryptionBegin, privatekey);

            serverId = new BigInteger(
                    (byte[]) minecraftEncryption$generateServerIdMethod.invoke("", keyPair.getPublic(), secretkey)
            ).toString(16);

            stateFieldSetter.invoke(loginListener, enumProtocolState$AUTHENTICATING);
            NetworkManager$setupEncryption.invoke(this.connection, secretkey);
        } catch (CryptographyException cryptographyexception) {
            throw new IllegalStateException("Protocol error", cryptographyexception);
        }

        multiCoreAPI.getPlugin().getRunServer().getScheduler().runTaskAsync(()-> {
            InetAddress address = getAddress();
            AuthResult authResult = multiCoreAPI.getAuthHandler().auth(gameProfile.getName(), serverId, address == null ? null : address.getHostAddress());
//            if (!connection.h()) {
//                return;
//            }
            try {
                if (authResult.isAllowed()) {
                    GameProfile profile = generateGameProfile(authResult.getResponse());
                    gameProfileFieldSetter.invoke(loginListener, profile);
                    fireEvent();
                } else {
                    loginListener.disconnect(authResult.getKickMessage());
                }
            } catch (Throwable e) {
                loginListener.disconnect(multiCoreAPI.getLanguageHandler().getMessage("auth_error"));
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

    private void fireEvent() throws Throwable {
        Class<LoginListener.LoginHandler> handlerClass = LoginListener.LoginHandler.class;
        LoginListener.LoginHandler handler = (LoginListener.LoginHandler) MethodHandles.lookup()
                .unreflectConstructor(
                        ReflectUtil.handleAccessible(
                                handlerClass.getConstructor(LoginListener.class)))
                .invoke(loginListener);
        handler.fireEvents();
    }
}
