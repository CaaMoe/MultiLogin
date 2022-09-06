package moe.caa.multilogin.bukkit.injector;

import com.mojang.authlib.minecraft.MinecraftSessionService;
import lombok.Getter;
import moe.caa.multilogin.api.injector.Injector;
import moe.caa.multilogin.api.main.MultiCoreAPI;
import moe.caa.multilogin.api.util.reflect.EnumAccessor;
import moe.caa.multilogin.api.util.reflect.ReflectUtil;
import moe.caa.multilogin.bukkit.injector.proxy.MinecraftSessionServiceInvocationHandler;
import moe.caa.multilogin.bukkit.injector.proxy.PacketLoginInEncryptionBeginInvocationHandler;
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

public class BukkitInjector implements Injector {
    @Getter
    private static MultiCoreAPI api;
    @Getter
    private static String nmsVersion;
    @Getter
    private static Enum<?> enumProtocol_HANDSHAKING;
    @Getter
    private static Enum<?> enumProtocol_PLAY;
    @Getter
    private static Enum<?> enumProtocol_STATUS;
    @Getter
    private static Enum<?> enumProtocol_LOGIN;
    @Getter
    private static Enum<?> enumProtocolDirection_SERVERBOUND;
    @Getter
    private static Enum<?> enumProtocolDirection_CLIENTBOUND;
    @Getter
    private static Class<?> packetClass;
    @Getter
    private static Class<?> loginListenerClass;
    @Getter
    private static Class<?> packetListenerClass;
    @Getter
    private static Class<?> iChatBaseComponentClass;
    @Getter
    private static Class<?> minecraftServerClass;
    @Getter
    private static Class<?> minecraftSessionServiceClass;
    @Getter
    private static Class<?> dedicatedPlayerListClass;
    @Getter
    private static Class<?> playerListClass;
    @Getter
    private static Class<?> dedicatedServerClass;
    @Getter
    private static Class<?> iChatBaseComponent$chatSerializerClass;
    @Getter
    private static Class<?> iChatMutableComponentClass;

    private void initReflectData() throws ClassNotFoundException {

        packetClass = InjectUtil.findNMSClass("Packet", "network.protocol", nmsVersion);
        loginListenerClass = InjectUtil.findNMSClass("LoginListener", "server.network", nmsVersion);
        packetListenerClass = InjectUtil.findNMSClass("PacketListener", "network", nmsVersion);
        iChatBaseComponentClass = InjectUtil.findNMSClass("IChatBaseComponent", "network.chat", nmsVersion);
        minecraftServerClass = InjectUtil.findNMSClass("MinecraftServer", "server", nmsVersion);
        minecraftSessionServiceClass = MinecraftSessionService.class;

        dedicatedPlayerListClass = InjectUtil.findNMSClass("DedicatedPlayerList", "server.dedicated", nmsVersion);
        playerListClass = InjectUtil.findNMSClass("PlayerList", "server.players", nmsVersion);
        dedicatedServerClass = InjectUtil.findNMSClass("DedicatedServer", "server.dedicated", nmsVersion);
        iChatBaseComponent$chatSerializerClass = Class.forName(iChatBaseComponentClass.getName() + "$ChatSerializer");
        iChatMutableComponentClass = InjectUtil.findNMSClass("IChatMutableComponent", "network.chat", nmsVersion);

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
        BukkitInjector.api = api;
        nmsVersion = ((MultiLoginBukkit) api.getPlugin())
                .getServer().getClass().getName().split("\\.")[3];

        try {
            initReflectData();
            PacketLoginInEncryptionBeginInvocationHandler.init();
            if (!InjectUtil.redirectInput(enumProtocol_LOGIN, enumProtocolDirection_SERVERBOUND, 0x01, var0 -> {
                if (var0 instanceof Function) {
                    Function<?, ?> function = (Function<?, ?>) var0;
                    // 返回代理数据包对象
                    return Proxy.newProxyInstance(Bukkit.class.getClassLoader(), new Class[]{Function.class}, new FixedReturnParameterInvocationHandler(
                            function, m -> m.getName().equals("apply"),
                            // 返回代理数据包处理类对象
                            o -> Proxy.newProxyInstance(Bukkit.class.getClassLoader(),
                                    new Class[]{packetClass}, new PacketLoginInEncryptionBeginInvocationHandler(o)
                            )
                    ));
                } else if (var0 instanceof Supplier) {
                    Supplier<?> supplier = ((Supplier<?>) var0);
                    return Proxy.newProxyInstance(Bukkit.class.getClassLoader(), new Class[]{Supplier.class}, new FixedReturnParameterInvocationHandler(
                            supplier, m -> m.getName().equals("get"),
                            // 返回代理数据包处理类对象
                            o -> Proxy.newProxyInstance(Bukkit.class.getClassLoader(),
                                    new Class[]{packetClass}, new PacketLoginInEncryptionBeginInvocationHandler(o)
                            )
                    ));
                }
                throw new RuntimeException(var0.getClass().getName());
            })) {
                throw new RuntimeException("0x01 -> new MultiPacketLoginInEncryptionBegin");
            }


            redirectHasJoined();
//            NMSHandlerEnum handlerEnum = NMSHandlerEnum.valueOf(nmsVersion.toLowerCase(Locale.ROOT));
//            Injector injector = (Injector) Class.forName(handlerEnum.getNhc()).getConstructor().newInstance();
//            injector.inject(api);
        } catch (Throwable t0) {
            throw new RuntimeException("Servers with Bukkit version " + nmsVersion + " are not supported.", t0);
        }
    }

    private void redirectHasJoined() throws NoSuchFieldException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        Object minecraftServer = getMinecraftServerObject();
        try {
            Field field = ReflectUtil.handleAccessible(ReflectUtil.findNoStaticField(minecraftServerClass, minecraftSessionServiceClass));
            MinecraftSessionService service = (MinecraftSessionService) field.get(minecraftServer);
            field.set(minecraftServer,
                    Proxy.newProxyInstance(Bukkit.class.getClassLoader(), new Class[]{minecraftSessionServiceClass}, new MinecraftSessionServiceInvocationHandler(service)));
            return;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private Object getMinecraftServerObject() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        Server server = ((MultiLoginBukkit) api.getPlugin()).getServer();
        Field playerListField = ReflectUtil.handleAccessible(ReflectUtil.findNoStaticField(server.getClass(), dedicatedPlayerListClass));
        Object dedicatedPlayerList = playerListField.get(server);

        Field serverField = ReflectUtil.handleAccessible(ReflectUtil.findNoStaticField(playerListClass, minecraftServerClass));
        return serverField.get(dedicatedPlayerList);
    }
}
