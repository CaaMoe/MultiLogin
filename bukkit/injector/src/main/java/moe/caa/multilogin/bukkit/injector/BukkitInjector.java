package moe.caa.multilogin.bukkit.injector;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import lombok.Getter;
import moe.caa.multilogin.api.function.ThrowFunction;
import moe.caa.multilogin.api.injector.Injector;
import moe.caa.multilogin.api.main.MultiCoreAPI;
import moe.caa.multilogin.api.util.reflect.EnumAccessor;
import moe.caa.multilogin.api.util.reflect.ReflectUtil;
import moe.caa.multilogin.bukkit.injector.data.LoginListenerData;
import moe.caa.multilogin.bukkit.injector.proxy.MinecraftSessionServiceInvocationHandler;
import moe.caa.multilogin.bukkit.injector.proxy.SignatureValidatorInvocationHandler;
import moe.caa.multilogin.bukkit.injector.redefine.loginlistener.LoginListenerRedirectHandler;
import moe.caa.multilogin.bukkit.main.MultiLoginBukkit;
import org.bukkit.Bukkit;
import org.bukkit.Server;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.net.SocketAddress;

/**
 * Bukkit 的注入程序
 */
@Getter
public class BukkitInjector implements Injector {
    @Getter
    private static BukkitInjector injector;
    private final LoginListenerRedirectHandler loginListenerRedirectHandler = new LoginListenerRedirectHandler();
    private final LoginListenerData loginListenerData = new LoginListenerData();
    private MultiCoreAPI api;
    private String nmsVersion;
    private Enum<?> enumProtocol_HANDSHAKING;
    private Enum<?> enumProtocol_PLAY;
    private Enum<?> enumProtocol_STATUS;
    private Enum<?> enumProtocol_LOGIN;
    private Enum<?> enumProtocolDirection_SERVERBOUND;
    private Enum<?> enumProtocolDirection_CLIENTBOUND;
    private Class<?> packetClass;
    private Class<?> loginListenerClass;
    private Class<?> packetLoginInEncryptionBeginClass;
    private Class<?> packetListenerClass;
    private Class<?> iChatBaseComponentClass;
    private Class<?> minecraftServerClass;
    private Class<?> minecraftSessionServiceClass;
    private Class<?> dedicatedPlayerListClass;
    private Class<?> playerListClass;
    private Class<?> dedicatedServerClass;
    private Class<?> packetLoginInListenerClass;
    private Class<?> networkManagerClass;
    // Nullable
    private Class<?> chatComponentTextClass;
    // Nullable
    private Class<?> craftChatMessageClass;
    private ThrowFunction<String, Object> functionGenerateLiteralTextComponent;
    private MethodHandle loginListener_networkManagerGetter;
    private MethodHandle loginListener_socketAddressGetter;
    private MethodHandle loginListener_gameProfileGetter;

    /**
     * 初始化全部数据
     */
    private void initValue() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, NoSuchFieldException {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        packetClass = InjectUtil.findNMSClass("Packet", "network.protocol", nmsVersion);
        loginListenerClass = InjectUtil.findNMSClass("LoginListener", "server.network", nmsVersion);
        packetListenerClass = InjectUtil.findNMSClass("PacketListener", "network", nmsVersion);
        iChatBaseComponentClass = InjectUtil.findNMSClass("IChatBaseComponent", "network.chat", nmsVersion);
        minecraftServerClass = InjectUtil.findNMSClass("MinecraftServer", "server", nmsVersion);
        minecraftSessionServiceClass = MinecraftSessionService.class;
        packetLoginInEncryptionBeginClass = InjectUtil.findNMSClass("PacketLoginInEncryptionBegin", "network.protocol.login", nmsVersion);
        packetLoginInListenerClass = InjectUtil.findNMSClass("PacketLoginInListener", "network.protocol.login", nmsVersion);
        networkManagerClass = InjectUtil.findNMSClass("NetworkManager", "network", nmsVersion);

        try {
            chatComponentTextClass = InjectUtil.findNMSClass("ChatComponentText", "network.chat", nmsVersion);
            MethodHandle handle = lookup.unreflectConstructor(chatComponentTextClass.getConstructor(String.class));
            functionGenerateLiteralTextComponent = handle::invoke;
        } catch (Throwable throwable) {
            craftChatMessageClass = InjectUtil.findOBCClass("CraftChatMessage", "util", nmsVersion);
            MethodHandle handle = lookup.unreflect(
                    ReflectUtil.handleAccessible(
                            craftChatMessageClass.getDeclaredMethod("fromStringOrNull", String.class)
                    )
            );
            functionGenerateLiteralTextComponent = handle::invoke;
        }

        dedicatedPlayerListClass = InjectUtil.findNMSClass("DedicatedPlayerList", "server.dedicated", nmsVersion);
        playerListClass = InjectUtil.findNMSClass("PlayerList", "server.players", nmsVersion);
        dedicatedServerClass = InjectUtil.findNMSClass("DedicatedServer", "server.dedicated", nmsVersion);

        EnumAccessor enumProtocolAccessor = new EnumAccessor(InjectUtil.findNMSClass("EnumProtocol", "network", nmsVersion));
        EnumAccessor enumProtocolDirectionAccessor = new EnumAccessor(InjectUtil.findNMSClass("EnumProtocolDirection", "network.protocol", nmsVersion));

        enumProtocol_HANDSHAKING = enumProtocolAccessor.indexOf(0);
        enumProtocol_PLAY = enumProtocolAccessor.indexOf(1);
        enumProtocol_STATUS = enumProtocolAccessor.indexOf(2);
        enumProtocol_LOGIN = enumProtocolAccessor.indexOf(3);

        enumProtocolDirection_SERVERBOUND = enumProtocolDirectionAccessor.indexOf(0);
        enumProtocolDirection_CLIENTBOUND = enumProtocolDirectionAccessor.indexOf(1);

        loginListener_gameProfileGetter = lookup.unreflectGetter(ReflectUtil.handleAccessible(ReflectUtil.findNoStaticField(injector.getLoginListenerClass(), GameProfile.class)));
        loginListener_networkManagerGetter = lookup.unreflectGetter(ReflectUtil.handleAccessible(ReflectUtil.findNoStaticField(injector.getLoginListenerClass(), injector.getNetworkManagerClass())));
        loginListener_socketAddressGetter = lookup.unreflectGetter(ReflectUtil.handleAccessible(ReflectUtil.findNoStaticField(injector.getNetworkManagerClass(), SocketAddress.class)));
    }

    /**
     * 注入方法开始
     */
    @Override
    public void inject(MultiCoreAPI api) {
        injector = this;
        this.api = api;
        nmsVersion = ((MultiLoginBukkit) api.getPlugin())
                .getServer().getClass().getName().split("\\.")[3];

        try {
            initValue();
            loginListenerRedirectHandler.init();

            redirectHasJoined();
            redirectFakeSignatureValidator();
        } catch (Throwable t0) {
            throw new RuntimeException("Servers with Bukkit version " + nmsVersion + " are not supported.", t0);
        }
    }

    /**
     * 提交假的签名验证器
     */
    private void redirectFakeSignatureValidator() throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException {
        Object minecraftServer = getMinecraftServerObject();
        Class<?> signatureValidatorClass;
        try {
            signatureValidatorClass = Class.forName("net.minecraft.util.SignatureValidator");
        } catch (Exception ignore) {
            return;
        }
        Class<?> servicesClass = InjectUtil.findNMSClass("Services", "server", nmsVersion);
        Field servicesField = ReflectUtil.handleAccessible(ReflectUtil.findNoStaticField(minecraftServerClass, servicesClass));
        Object serviceObj = servicesField.get(minecraftServer);
        serviceObj = ReflectUtil.redirectRecordObject(serviceObj,
                o1 -> o1.getClass().getName().contains("SignatureValidator"),
                o12 -> Proxy.newProxyInstance(Bukkit.class.getClassLoader(),
                        new Class[]{signatureValidatorClass}, new SignatureValidatorInvocationHandler(o12))
        );
        servicesField.set(minecraftServer, serviceObj);
    }

    /**
     * 重定向 HasJoined
     */
    private void redirectHasJoined() throws NoSuchFieldException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, ClassNotFoundException, InstantiationException {
        Object minecraftServer = getMinecraftServerObject();
        try {
            Field field = ReflectUtil.handleAccessible(ReflectUtil.findNoStaticField(minecraftServerClass, minecraftSessionServiceClass));
            MinecraftSessionService service = (MinecraftSessionService) field.get(minecraftServer);
            field.set(minecraftServer,
                    Proxy.newProxyInstance(Bukkit.class.getClassLoader(), new Class[]{minecraftSessionServiceClass}, new MinecraftSessionServiceInvocationHandler(this, service)));
        } catch (Throwable e) {
            Class<?> servicesClass = InjectUtil.findNMSClass("Services", "server", nmsVersion);
            Field servicesField = ReflectUtil.handleAccessible(ReflectUtil.findNoStaticField(minecraftServerClass, servicesClass));
            Object serviceObj = servicesField.get(minecraftServer);

            serviceObj = ReflectUtil.redirectRecordObject(serviceObj,
                    o1 -> o1.getClass().getName().contains("SessionService"),
                    o12 -> Proxy.newProxyInstance(Bukkit.class.getClassLoader(),
                            new Class[]{minecraftSessionServiceClass}, new MinecraftSessionServiceInvocationHandler(this, (MinecraftSessionService) o12))
            );

            servicesField.set(minecraftServer, serviceObj);
        }
    }

    public Object getMinecraftServerObject() throws IllegalAccessException, NoSuchFieldException {
        Server server = ((MultiLoginBukkit) api.getPlugin()).getServer();
        Field playerListField = ReflectUtil.handleAccessible(ReflectUtil.findNoStaticField(server.getClass(), dedicatedPlayerListClass));
        Object dedicatedPlayerList = playerListField.get(server);

        Field serverField = ReflectUtil.handleAccessible(ReflectUtil.findNoStaticField(playerListClass, minecraftServerClass));
        return serverField.get(dedicatedPlayerList);
    }

    public Object generateIChatBaseComponent(String text) throws Throwable {
        return functionGenerateLiteralTextComponent.apply(text);
    }
}
