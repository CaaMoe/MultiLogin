package moe.caa.multilogin.velocity.injector;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.StateRegistry;
import com.velocitypowered.proxy.protocol.packet.EncryptionResponsePacket;
import com.velocitypowered.proxy.protocol.packet.ServerLoginPacket;
import io.netty.util.collection.IntObjectMap;
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
import java.util.*;
import java.util.function.Supplier;

import static com.google.common.collect.Iterables.getLast;
import static com.velocitypowered.api.network.ProtocolVersion.SUPPORTED_VERSIONS;

/**
 * Velocity 注入程序
 */
public class VelocityInjector implements Injector {

    @Override
    public void inject(MultiCoreAPI multiCoreAPI) throws NoSuchFieldException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, NoSuchEnumException {
        MultiInitialLoginSessionHandler.init();
        // auth
        {
            StateRegistry.PacketRegistry serverbound = getServerboundPacketRegistry(StateRegistry.LOGIN);
            redirectInput(serverbound, EncryptionResponsePacket.class, () -> new MultiEncryptionResponse(multiCoreAPI));
            redirectInput(serverbound, ServerLoginPacket.class, () -> new MultiServerLogin(multiCoreAPI));
        }
    }

    public void registerChatSession(Map<Integer,Integer> packetMapping) {
        // chat
        try {
            StateRegistry.PacketRegistry serverbound = getServerboundPacketRegistry(StateRegistry.PLAY);

            LinkedList<StateRegistry.PacketMapping> playerSessionPacketMapping = new LinkedList<>();
            for (Map.Entry<Integer, Integer> entry : packetMapping.entrySet()) {
                LoggerProvider.getLogger().debug("Register PlayerSessionPacketBlocker for protocol version: " + entry.getKey());
                playerSessionPacketMapping.add(createPacketMapping(entry.getValue(), ProtocolVersion.getProtocolVersion(entry.getKey()), false));
            }
            registerPacket(serverbound, PlayerSessionPacketBlocker.class, PlayerSessionPacketBlocker::new, playerSessionPacketMapping.toArray(new StateRegistry.PacketMapping[0]));

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
        return getProtocolRegistriesMap(bound).values();
    }

    private Map<?, ?> getProtocolRegistriesMap(StateRegistry.PacketRegistry bound) throws NoSuchFieldException, IllegalAccessException {
        Field f$versions = StateRegistry.PacketRegistry.class.getDeclaredField("versions");
        ReflectUtil.handleAccessible(f$versions);

        //Map<ProtocolVersion, ProtocolRegistry> versions;
        return (Map<?, ?>) f$versions.get(bound);
    }

    private StateRegistry.PacketMapping createPacketMapping(int id, ProtocolVersion protocolVersion, ProtocolVersion lastValidProtocolVersion, boolean packetDecoding) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Constructor<StateRegistry.PacketMapping> constructor =  ReflectUtil.handleAccessible(StateRegistry.PacketMapping.class.
                getDeclaredConstructor(int.class, ProtocolVersion.class, ProtocolVersion.class, boolean.class));
        return constructor.newInstance(id, protocolVersion, lastValidProtocolVersion, packetDecoding);
    }

    private StateRegistry.PacketMapping createPacketMapping(int id, ProtocolVersion protocolVersion, boolean packetDecoding) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        return createPacketMapping(id, protocolVersion, null, packetDecoding);
    }

    private <P extends MinecraftPacket> void registerPacket(StateRegistry.PacketRegistry packetRegistry, Class<P> clazz, Supplier<P> packetSupplier, StateRegistry.PacketMapping[] mappings) throws IllegalAccessException {
        //Method register = ReflectUtil.handleAccessible(packetRegistry.getClass().getDeclaredMethod("register", Class.class, Supplier.class, StateRegistry.PacketMapping[].class));
        //register.invoke(packetRegistry, clazz, packetSupplier, mappings);
        try {
            register(packetRegistry,clazz,packetSupplier,mappings);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    <P extends MinecraftPacket> void register(StateRegistry.PacketRegistry bound,Class<P> clazz, Supplier<P> packetSupplier,
                                              StateRegistry.PacketMapping... mappings) throws NoSuchFieldException, IllegalAccessException {
        if (mappings.length == 0) {
            throw new IllegalArgumentException("At least one mapping must be provided.");
        }

        for (int i = 0; i < mappings.length; i++) {
            StateRegistry.PacketMapping current = mappings[i];
            StateRegistry.PacketMapping next = (i + 1 < mappings.length) ? mappings[i + 1] : current;

            Field protocolVersion = current.getClass().getDeclaredField("protocolVersion");
            protocolVersion.setAccessible(true);
            ProtocolVersion from = (ProtocolVersion) protocolVersion.get(current);
            Field lastValidProtocolVersion = current.getClass().getDeclaredField("lastValidProtocolVersion");
            lastValidProtocolVersion.setAccessible(true);
            ProtocolVersion lastValid = (ProtocolVersion) lastValidProtocolVersion.get(current);
            if (lastValid != null) {
                if (next != current) {
                    throw new IllegalArgumentException("Cannot add a mapping after last valid mapping");
                }
                if (from.greaterThan(lastValid)) {
                    throw new IllegalArgumentException(
                            "Last mapping version cannot be higher than highest mapping version");
                }
            }
            Field nextProtocolVersion = next.getClass().getDeclaredField("protocolVersion");
            nextProtocolVersion.setAccessible(true);
            ProtocolVersion to = current == next ? lastValid != null
                    ? lastValid : getLast(SUPPORTED_VERSIONS) : (ProtocolVersion) nextProtocolVersion.get(next);

            ProtocolVersion lastInList = lastValid != null ? lastValid : getLast(SUPPORTED_VERSIONS);

            if (from.noLessThan(to) && from != lastInList) {
                throw new IllegalArgumentException(String.format(
                        "Next mapping version (%s) should be lower then current (%s)", to, from));
            }

            for (ProtocolVersion protocol : EnumSet.range(from, to)) {
                if (protocol == to && next != current) {
                    break;
                }
                StateRegistry.PacketRegistry.ProtocolRegistry registry = (
                        (StateRegistry.PacketRegistry.ProtocolRegistry)
                                getProtocolRegistriesMap(bound).get(protocol)
                );

                if (registry == null) {
                    throw new IllegalArgumentException(
                            "Unknown protocol version " + protocolVersion);
                }

                Field packetIdToSupplier = registry.getClass().getDeclaredField("packetIdToSupplier");
                packetIdToSupplier.setAccessible(true);
                IntObjectMap<Supplier<? extends MinecraftPacket>> supplierIntObjectMap = (IntObjectMap<Supplier<? extends MinecraftPacket>>) packetIdToSupplier.get(registry);
                Field idField = current.getClass().getDeclaredField("id");
                idField.setAccessible(true);
                if (supplierIntObjectMap.containsKey(idField.getInt(current))) {
                    continue;
                    /*
                    throw new IllegalArgumentException(
                            "Can not register class "
                                    + clazz.getSimpleName()
                                    + " with id "
                                    + current.id
                                    + " for "
                                    + registry.version
                                    + " because another packet is already registered");
                     */
                }

                Field packetClassToIdField = registry.getClass().getDeclaredField("packetClassToId");
                packetClassToIdField.setAccessible(true);
                Map<Class<? extends MinecraftPacket>, Integer> packetClassToId = (Map<Class<? extends MinecraftPacket>, Integer>) packetClassToIdField.get(registry);
                if (packetClassToId.containsKey(clazz)) {
                    throw new IllegalArgumentException(
                            clazz.getSimpleName() + " is already registered for version " + registry.version);
                }

                Field encodeOnly = current.getClass().getDeclaredField("encodeOnly");
                encodeOnly.setAccessible(true);
                if (!encodeOnly.getBoolean(current)) {
                    supplierIntObjectMap.put(idField.getInt(current), packetSupplier);
                }
                packetClassToId.put(clazz, idField.getInt(current));
            }
        }
    }
}
