package moe.caa.multilogin.velocity.injector;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.StateRegistry;
import com.velocitypowered.proxy.protocol.packet.EncryptionResponsePacket;
import com.velocitypowered.proxy.protocol.packet.ServerLoginPacket;
import moe.caa.multilogin.api.internal.injector.Injector;
import moe.caa.multilogin.api.internal.logger.LoggerProvider;
import moe.caa.multilogin.api.internal.main.MultiCoreAPI;
import moe.caa.multilogin.api.internal.util.reflect.NoSuchEnumException;
import moe.caa.multilogin.api.internal.util.reflect.ReflectUtil;
import moe.caa.multilogin.velocity.injector.handler.MultiInitialLoginSessionHandler;
import moe.caa.multilogin.velocity.injector.redirect.auth.MultiEncryptionResponse;
import moe.caa.multilogin.velocity.injector.redirect.auth.MultiServerLogin;
import moe.caa.multilogin.velocity.injector.redirect.chat.PlayerSessionPacketBlocker;

import java.lang.reflect.Constructor;
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
    public void inject(MultiCoreAPI multiCoreAPI) throws NoSuchFieldException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, NoSuchEnumException, InstantiationException {
        MultiInitialLoginSessionHandler.init();

        // auth
        {
            StateRegistry.PacketRegistry serverbound = getServerboundPacketRegistry(StateRegistry.LOGIN);
            redirectInput(serverbound, EncryptionResponsePacket.class, () -> new MultiEncryptionResponse(multiCoreAPI));
            redirectInput(serverbound, ServerLoginPacket.class, () -> new MultiServerLogin(multiCoreAPI));
        }

        // chat
        try {
            StateRegistry.PacketRegistry serverbound = getServerboundPacketRegistry(StateRegistry.PLAY);
            StateRegistry.PacketMapping[] playerSessionPacketMapping = {
                    createPacketMapping(0x20, ProtocolVersion.MINECRAFT_1_19_3, false),
                    createPacketMapping(0x06, ProtocolVersion.MINECRAFT_1_19_4, false),
                    createPacketMapping(0x07, ProtocolVersion.MINECRAFT_1_20_5, false)
            };
            registerPacket(serverbound, PlayerSessionPacketBlocker.class, PlayerSessionPacketBlocker::new, playerSessionPacketMapping);
        } catch (Throwable throwable){
            LoggerProvider.getLogger().error("Unable to register PlayerSessionPacketBlocker, chat session blocker does not work as expected.", throwable);
        }
    }

    private StateRegistry.PacketRegistry getServerboundPacketRegistry(StateRegistry stateRegistry) throws NoSuchFieldException, IllegalAccessException {
        Field serverboundField = ReflectUtil.handleAccessible(StateRegistry.class.getDeclaredField("serverbound"));
        return  (StateRegistry.PacketRegistry) serverboundField.get(stateRegistry);
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

    private StateRegistry.PacketMapping createPacketMapping(int id, ProtocolVersion protocolVersion, ProtocolVersion lastValidProtocolVersion, boolean packetDecoding) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Constructor<StateRegistry.PacketMapping> constructor =  ReflectUtil.handleAccessible(StateRegistry.PacketMapping.class.
                getDeclaredConstructor(int.class, ProtocolVersion.class, ProtocolVersion.class, boolean.class));
        return constructor.newInstance(id, protocolVersion, lastValidProtocolVersion, packetDecoding);
    }

    private StateRegistry.PacketMapping createPacketMapping(int id, ProtocolVersion protocolVersion, boolean packetDecoding) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        return createPacketMapping(id, protocolVersion, null, packetDecoding);
    }

    private <P extends MinecraftPacket> void registerPacket(StateRegistry.PacketRegistry packetRegistry, Class<P> clazz, Supplier<P> packetSupplier, StateRegistry.PacketMapping[] mappings) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method register = ReflectUtil.handleAccessible(packetRegistry.getClass().getDeclaredMethod("register", Class.class, Supplier.class, StateRegistry.PacketMapping[].class));
        register.invoke(packetRegistry, clazz, packetSupplier, mappings);
    }
}
