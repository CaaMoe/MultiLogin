package moe.caa.multilogin.bukkit.proxy;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.function.Supplier;

/**
 * net.minecraft.network.protocol.login.PacketLoginInEncryptionBegin
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PacketLoginInEncryptionBeginProxy implements MethodInterceptor{
    public static final Class<?> packetLoginInEncryptionClass = genPacketLoginInEncryptionClass();

    @SneakyThrows
    private static Class<?> genPacketLoginInEncryptionClass() {
        return Class.forName("net.minecraft.network.protocol.login.PacketLoginInListener");
    }

    private static Object createNewInstance() {
        PacketLoginInEncryptionBeginProxy proxy = new PacketLoginInEncryptionBeginProxy();
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(packetLoginInEncryptionClass);
        enhancer.setCallback(proxy);
        return enhancer.create();
    }

    public static Supplier<?> hand() {
        return PacketLoginInEncryptionBeginProxy::createNewInstance;
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        proxy.invoke(obj, args);
        return obj;
    }
}
