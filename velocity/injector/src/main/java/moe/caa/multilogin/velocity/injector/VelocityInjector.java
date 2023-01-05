package moe.caa.multilogin.velocity.injector;

import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.StateRegistry;
import com.velocitypowered.proxy.protocol.packet.EncryptionResponse;
import com.velocitypowered.proxy.protocol.packet.ServerLogin;
import moe.caa.multilogin.api.injector.Injector;
import moe.caa.multilogin.api.main.MultiCoreAPI;
import moe.caa.multilogin.api.util.reflect.NoSuchEnumException;
import moe.caa.multilogin.api.util.reflect.ReflectUtil;
import moe.caa.multilogin.velocity.injector.handler.MultiInitialLoginSessionHandler;
import moe.caa.multilogin.velocity.injector.redirect.MultiEncryptionResponse;
import moe.caa.multilogin.velocity.injector.redirect.MultiServerLogin;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Velocity 注入程序
 */
public class VelocityInjector implements Injector {

    @Override
    public void inject(MultiCoreAPI multiCoreAPI) throws NoSuchFieldException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, NoSuchEnumException {
        MultiInitialLoginSessionHandler.init();

        // auth
        StateRegistry stateRegistry = StateRegistry.LOGIN;
        Field serverboundField = ReflectUtil.handleAccessible(StateRegistry.class.getDeclaredField("serverbound"));
        StateRegistry.PacketRegistry serverbound = (StateRegistry.PacketRegistry) serverboundField.get(stateRegistry);
        redirectInput(serverbound, EncryptionResponse.class, () -> new MultiEncryptionResponse(multiCoreAPI));
        redirectInput(serverbound, ServerLogin.class, () -> new MultiServerLogin(multiCoreAPI));
    }

    /**
     * 重定向数据包
     *
     * @param bound            数据包方向
     * @param originalClass    原始数据包类对象
     * @param supplierRedirect 重定向后的 Supplier
     */
    private <T> void redirectInput(StateRegistry.PacketRegistry bound, Class<T> originalClass, Supplier<? extends T> supplierRedirect) throws NoSuchFieldException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Field f$packetIdToSupplier = StateRegistry.PacketRegistry.ProtocolRegistry.class.getDeclaredField("packetIdToSupplier");
        f$packetIdToSupplier.setAccessible(true);
        ReflectUtil.handleAccessible(f$packetIdToSupplier);


        Method map$entry$setValueMethod = Map.Entry.class.getMethod("setValue", Object.class);

        for (Object protocolRegistry : getProtocolRegistries(bound)) {
            Map<?, ?> packetIdToSupplier = (Map<?, ?>) f$packetIdToSupplier.get(protocolRegistry); // IntObjectMap<Supplier<? extends MinecraftPacket>>
            for (Map.Entry<?, ?> e : packetIdToSupplier.entrySet()) {
                MinecraftPacket minecraftPacketObject = (MinecraftPacket) ((Supplier<?>) e.getValue()).get();
                // 类匹配则进行替换
                if (minecraftPacketObject.getClass().equals(originalClass)) {
                    map$entry$setValueMethod.invoke(e, supplierRedirect);
                }
            }
        }
    }

    /**
     * 追加注册出口包
     *
     * @param bound         数据包方向
     * @param originalClass 原始数据包类对象
     * @param appendClass   追加的数据包类对象
     */
    private <T> void redirectOutput(StateRegistry.PacketRegistry bound, Class<T> originalClass, Class<? extends T> appendClass) throws NoSuchFieldException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Field f$packetClassToId = StateRegistry.PacketRegistry.ProtocolRegistry.class.getDeclaredField("packetClassToId");
        ReflectUtil.handleAccessible(f$packetClassToId);

        Method map$putMethod = Map.class.getMethod("put", Object.class, Object.class);

        for (Object protocolRegistry : getProtocolRegistries(bound)) {
            Map<?, ?> packetClassToId = (Map<?, ?>) f$packetClassToId.get(protocolRegistry);// Object2IntMap<Class<? extends MinecraftPacket>>
            if (!packetClassToId.containsKey(originalClass)) continue;
            map$putMethod.invoke(packetClassToId, appendClass, packetClassToId.get(originalClass));
        }
    }

    private Collection<?> getProtocolRegistries(StateRegistry.PacketRegistry bound) throws NoSuchFieldException, IllegalAccessException {
        Field f$versions = StateRegistry.PacketRegistry.class.getDeclaredField("versions");
        ReflectUtil.handleAccessible(f$versions);

        Map<?, ?> versionsObject = (Map<?, ?>) f$versions.get(bound);//Map<ProtocolVersion, ProtocolRegistry> versions;
        return versionsObject.values();
    }
}