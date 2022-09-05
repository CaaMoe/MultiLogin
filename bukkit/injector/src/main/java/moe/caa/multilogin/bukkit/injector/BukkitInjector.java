package moe.caa.multilogin.bukkit.injector;

import moe.caa.multilogin.api.injector.Injector;
import moe.caa.multilogin.api.main.MultiCoreAPI;
import moe.caa.multilogin.api.util.reflect.EnumAccessor;
import moe.caa.multilogin.bukkit.injector.proxy.FixedReturnParameterInvocationHandler;
import moe.caa.multilogin.bukkit.injector.proxy.InterceptMethodInvocationHandler;
import moe.caa.multilogin.bukkit.main.MultiLoginBukkit;
import org.bukkit.Bukkit;

import java.lang.reflect.Proxy;
import java.util.function.Function;

/**
 * Bukkit 的注入程序
 */
public class BukkitInjector implements Injector {
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
    private Class<?> packetListenerClass;

    private void initReflectData() throws ClassNotFoundException {

        packetClass = InjectUtil.findClass("Packet", "network.protocol", nmsVersion);
        loginListenerClass = InjectUtil.findClass("LoginListener", "server.network", nmsVersion);
        packetListenerClass = InjectUtil.findClass("PacketListener", "network", nmsVersion);


        EnumAccessor enumProtocolAccessor = new EnumAccessor(InjectUtil.findClass("EnumProtocol", "network", nmsVersion));
        EnumAccessor enumProtocolDirectionAccessor = new EnumAccessor(InjectUtil.findClass("EnumProtocolDirection", "network.protocol", nmsVersion));

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

            if (!InjectUtil.redirectInput(enumProtocol_LOGIN, enumProtocolDirection_SERVERBOUND, 0x01, var0 -> {
                if (var0 instanceof Function) {
                    Function<?, ?> function = (Function<?, ?>) var0;
                    return Proxy.newProxyInstance(Bukkit.class.getClassLoader(), new Class[]{Function.class}, new FixedReturnParameterInvocationHandler(
                            function, m -> m.getName().equals("apply"),
                            o -> Proxy.newProxyInstance(Bukkit.class.getClassLoader(),
                                    new Class[]{packetClass}, new InterceptMethodInvocationHandler(
                                            o, m -> m.getParameterTypes().length == 1 && m.getParameterTypes()[0].isAssignableFrom(packetListenerClass), (m, o1) -> {
                                        try {
                                            return m.invoke(o, o1);
                                        } catch (Throwable e) {
                                            throw new RuntimeException(e);
                                        }
                                    })
                            )
                    ));
                }

                return var0;
            })) {
                throw new RuntimeException("0x01 -> new MultiPacketLoginInEncryptionBegin");
            }

//            NMSHandlerEnum handlerEnum = NMSHandlerEnum.valueOf(nmsVersion.toLowerCase(Locale.ROOT));
//            Injector injector = (Injector) Class.forName(handlerEnum.getNhc()).getConstructor().newInstance();
//            injector.inject(api);
        } catch (Throwable t0) {
            throw new RuntimeException("Servers with Bukkit version " + nmsVersion + " are not supported.", t0);
        }

        throw new RuntimeException("Servers with Bukkit version " + nmsVersion + " are not supported.");
    }
}
