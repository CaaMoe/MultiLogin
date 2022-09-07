package moe.caa.multilogin.bukkit.injector;

import com.mojang.authlib.minecraft.MinecraftSessionService;
import lombok.Getter;
import moe.caa.multilogin.api.injector.Injector;
import moe.caa.multilogin.api.main.MultiCoreAPI;
import moe.caa.multilogin.api.util.reflect.EnumAccessor;
import moe.caa.multilogin.api.util.reflect.ReflectUtil;
import moe.caa.multilogin.bukkit.injector.proxy.MinecraftSessionServiceInvocationHandler;
import moe.caa.multilogin.bukkit.injector.proxy.SignatureValidatorInvocationHandler;
import moe.caa.multilogin.bukkit.injector.subclasshandler.LoginListenerSubclassHandler;
import moe.caa.multilogin.bukkit.injector.subclasshandler.PacketLoginInEncryptionBeginSubclassHandler;
import moe.caa.multilogin.bukkit.main.MultiLoginBukkit;
import moe.caa.multilogin.core.proxy.FixedReturnParameterInvocationHandler;
import org.bukkit.Bukkit;
import org.bukkit.Server;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Bukkit 的注入程序
 */
@Getter
public class BukkitInjector implements Injector {
    private final PacketLoginInEncryptionBeginSubclassHandler packetLoginInEncryptionBeginSubclassHandler = new PacketLoginInEncryptionBeginSubclassHandler(this);
    private final LoginListenerSubclassHandler loginListenerSubclassHandler = new LoginListenerSubclassHandler(this);
    private final LoginListenerSynchronizer loginListenerSynchronizer = new LoginListenerSynchronizer(this);
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
    private Class<?> iChatBaseComponent$chatSerializerClass;
    private Class<?> chatComponentTextClass;
    private Class<?> packetLoginInListenerClass;

    private void initReflectData() throws ClassNotFoundException {
        packetClass = InjectUtil.findNMSClass("Packet", "network.protocol", nmsVersion);
        loginListenerClass = InjectUtil.findNMSClass("LoginListener", "server.network", nmsVersion);
        packetListenerClass = InjectUtil.findNMSClass("PacketListener", "network", nmsVersion);
        iChatBaseComponentClass = InjectUtil.findNMSClass("IChatBaseComponent", "network.chat", nmsVersion);
        minecraftServerClass = InjectUtil.findNMSClass("MinecraftServer", "server", nmsVersion);
        minecraftSessionServiceClass = MinecraftSessionService.class;
        packetLoginInEncryptionBeginClass = InjectUtil.findNMSClass("PacketLoginInEncryptionBegin", "network.protocol.login", nmsVersion);
        packetLoginInListenerClass = InjectUtil.findNMSClass("PacketLoginInListener", "network.protocol.login", nmsVersion);
        chatComponentTextClass = InjectUtil.findNMSClass("ChatComponentText", "network.chat", nmsVersion);

        dedicatedPlayerListClass = InjectUtil.findNMSClass("DedicatedPlayerList", "server.dedicated", nmsVersion);
        playerListClass = InjectUtil.findNMSClass("PlayerList", "server.players", nmsVersion);
        dedicatedServerClass = InjectUtil.findNMSClass("DedicatedServer", "server.dedicated", nmsVersion);
        iChatBaseComponent$chatSerializerClass = Class.forName(iChatBaseComponentClass.getName() + "$ChatSerializer");

        EnumAccessor enumProtocolAccessor = new EnumAccessor(InjectUtil.findNMSClass("EnumProtocol", "network", nmsVersion));
        EnumAccessor enumProtocolDirectionAccessor = new EnumAccessor(InjectUtil.findNMSClass("EnumProtocolDirection", "network.protocol", nmsVersion));

        enumProtocol_HANDSHAKING = enumProtocolAccessor.indexOf(0);
        enumProtocol_PLAY = enumProtocolAccessor.indexOf(1);
        enumProtocol_STATUS = enumProtocolAccessor.indexOf(2);
        enumProtocol_LOGIN = enumProtocolAccessor.indexOf(3);

        enumProtocolDirection_SERVERBOUND = enumProtocolDirectionAccessor.indexOf(0);
        enumProtocolDirection_CLIENTBOUND = enumProtocolDirectionAccessor.indexOf(1);
    }


    @Override
    public void inject(MultiCoreAPI api) {
        this.api = api;
        nmsVersion = ((MultiLoginBukkit) api.getPlugin())
                .getServer().getClass().getName().split("\\.")[3];

        try {
            initReflectData();
            packetLoginInEncryptionBeginSubclassHandler.init();
            loginListenerSubclassHandler.init();
            loginListenerSynchronizer.init();
            if (!InjectUtil.redirectInput(enumProtocol_LOGIN, enumProtocolDirection_SERVERBOUND, 0x01, this::getProxyPacketLoginInEncryptionBeginPacket)) {
                throw new RuntimeException("0x01 -> new MultiPacketLoginInEncryptionBegin");
            }

            redirectHasJoined();
            redirectFakeSignatureValidator();
        } catch (Throwable t0) {
            throw new RuntimeException("Servers with Bukkit version " + nmsVersion + " are not supported.", t0);
        }
    }

    private Object getProxyPacketLoginInEncryptionBeginPacket(Object obj) {
        if (obj instanceof Function) {
            Function<?, ?> function = (Function<?, ?>) obj;
            // 返回代理数据包对象
            return Proxy.newProxyInstance(Bukkit.class.getClassLoader(), new Class[]{Function.class}, new FixedReturnParameterInvocationHandler(
                    function, m -> m.getName().equals("apply"),
                    (handle, invokeArgs) -> packetLoginInEncryptionBeginSubclassHandler.newProxyLoginInEncryptionBegin((Object[]) invokeArgs)
            ));
        } else if (obj instanceof Supplier) {
            Supplier<?> supplier = ((Supplier<?>) obj);
            return Proxy.newProxyInstance(Bukkit.class.getClassLoader(), new Class[]{Supplier.class}, new FixedReturnParameterInvocationHandler(
                    supplier, m -> m.getName().equals("get"),
                    (handle, invokeArgs) -> packetLoginInEncryptionBeginSubclassHandler.newProxyLoginInEncryptionBegin()
            ));
        } else if (obj instanceof Class) {
            return packetLoginInEncryptionBeginSubclassHandler.getProxyLoginInEncryptionBeginClass();
        }
        throw new RuntimeException(obj.getClass().getName());
    }

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
                            new Class[]{minecraftServerClass}, new MinecraftSessionServiceInvocationHandler(this, (MinecraftSessionService) o12))
            );

            servicesField.set(minecraftServer, serviceObj);
        }
    }

    private Object getMinecraftServerObject() throws IllegalAccessException, NoSuchFieldException {
        Server server = ((MultiLoginBukkit) api.getPlugin()).getServer();
        Field playerListField = ReflectUtil.handleAccessible(ReflectUtil.findNoStaticField(server.getClass(), dedicatedPlayerListClass));
        Object dedicatedPlayerList = playerListField.get(server);

        Field serverField = ReflectUtil.handleAccessible(ReflectUtil.findNoStaticField(playerListClass, minecraftServerClass));
        return serverField.get(dedicatedPlayerList);
    }
}
