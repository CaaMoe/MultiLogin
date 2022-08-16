package moe.caa.multilogin.bukkit.injector.nms.v1_19_r1;

import moe.caa.multilogin.api.injector.Injector;
import moe.caa.multilogin.api.main.MultiCoreAPI;
import moe.caa.multilogin.api.util.ReflectUtil;
import moe.caa.multilogin.bukkit.injector.nms.v1_19_r1.handler.MultiPacketLoginInEncryptionBeginHandler;
import moe.caa.multilogin.bukkit.injector.nms.v1_19_r1.proxy.SignatureValidatorInvocationHandler;
import moe.caa.multilogin.bukkit.injector.nms.v1_19_r1.redirect.MultiPacketLoginInEncryptionBegin;
import moe.caa.multilogin.bukkit.main.MultiLoginBukkit;
import net.minecraft.network.EnumProtocol;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.EnumProtocolDirection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.util.SignatureValidator;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * v1_19_r1 注入
 */
public class NMSInjector implements Injector {

    @Override
    public void inject(MultiCoreAPI api) throws Throwable {
        MultiPacketLoginInEncryptionBeginHandler.init();
        if (!redirectInput(EnumProtocol.d, EnumProtocolDirection.a, 0x01, var0 -> new MultiPacketLoginInEncryptionBegin(var0, api))) {
            throw new RuntimeException("0x01 -> new MultiPacketLoginInEncryptionBegin");
        }

        DedicatedServer server = ((CraftServer) ((MultiLoginBukkit) api.getPlugin()).getServer()).getServer();

        Field servicesField = ReflectUtil.handleAccessible(ReflectUtil.findNoStaticField(MinecraftServer.class, Services.class));
        Object serviceObj  = servicesField.get(server);

        LinkedHashMap<Field, Object> fieldObjectMap = new LinkedHashMap<>();
        for (Field declaredField  : Services.class.getDeclaredFields()) {
            if (Modifier.isStatic(declaredField.getModifiers())) {
                continue;
            }
            Object value = ReflectUtil.handleAccessible(declaredField).get(serviceObj);
            if (value.getClass().getName().contains("SignatureValidator")) {
                value = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                        new Class[]{SignatureValidator.class}, new SignatureValidatorInvocationHandler(value));
            }
            fieldObjectMap.put(declaredField, value);
        }

        final Constructor<?> declaredConstructor = serviceObj.getClass().getDeclaredConstructor(
                fieldObjectMap.keySet().stream().map(Field::getType).toArray(Class[]::new)
        );

        final Object o = declaredConstructor.newInstance(fieldObjectMap.values().toArray());

        servicesField.set(server, o);
    }

    /**
     * 重定向数据包
     *
     * @param bound            协议阶段
     * @param direction        数据包方向
     * @param packetId         数据包 ID
     * @param functionRedirect 重定向后的 Function
     */
    private boolean redirectInput(EnumProtocol bound, EnumProtocolDirection direction, int packetId, Function<PacketDataSerializer, ? extends Packet<?>> functionRedirect) throws Throwable {
        // Map<NetworkSide, ? extends PacketHandler<?>>;
        Field j = ReflectUtil.findNoStaticField(bound.getClass(), Map.class);
        Map<?, ?> packetHandlers = (Map<?, ?>) ReflectUtil.handleAccessible(j).get(bound);

        Object packetHandler = packetHandlers.get(direction);
        // 如果没有，代表发包方向不一样
        if (packetHandler == null) {
            return false;
        }

        // List<Function<PacketByteBuf, ? extends Packet<T>>> packetFactories
        Field c = ReflectUtil.findNoStaticField(packetHandler.getClass(), List.class);
        List<?> packetFactories = (List<?>) ReflectUtil.handleAccessible(c).get(packetHandler);

        List.class.getDeclaredMethod("set", int.class, Object.class).invoke(
                packetFactories, packetId, functionRedirect
        );
        return true;
    }
}
