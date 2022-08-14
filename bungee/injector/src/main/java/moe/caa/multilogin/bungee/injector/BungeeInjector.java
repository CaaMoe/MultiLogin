package moe.caa.multilogin.bungee.injector;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TObjectIntMap;
import moe.caa.multilogin.api.injector.Injector;
import moe.caa.multilogin.api.main.MultiCoreAPI;
import moe.caa.multilogin.bungee.injector.redirect.MultiEncryptionResponse;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.packet.EncryptionResponse;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Supplier;

/**
 * Bungee 的注入器
 */
public class BungeeInjector implements Injector {
    @Override
    public void inject(MultiCoreAPI api) throws Throwable {
        redirectIn(Protocol.LOGIN, EncryptionResponse.class, MultiEncryptionResponse::new);
    }

    private synchronized <T> void redirectIn(Protocol protocol, Class<T> originalClass, Supplier<? extends T> redirectSupplier) throws NoSuchFieldException, ClassNotFoundException, IllegalAccessException {
        Class<?> c$protocolData = Class.forName("net.md_5.bungee.protocol.Protocol$ProtocolData");
        Field f$packetConstructors = c$protocolData.getDeclaredField("packetConstructors");// Supplier<? extends DefinedPacket>[]
        accessSet(f$packetConstructors);

        // 遍历所有版本协议
        for (Object data : getProtocolDataList(protocol, getServerSideField())) {//ProtocolData
            // 获取其入口包构造数据
            Supplier<?>[] definedPackets = (Supplier<?>[]) (f$packetConstructors.get(data)); // ? extends DefinedPacket
//            寻找该类
            for (int i = 0; i < definedPackets.length; i++) {
                if (definedPackets[i] == null) continue;
                if (definedPackets[i].get().getClass().equals(originalClass)) {
                    definedPackets[i] = redirectSupplier;
                }
            }
        }
    }


    private synchronized <T, F> void redirectOut(Protocol protocol, Class<T> originalClass, Class<F> redirectClass) throws NoSuchFieldException, ClassNotFoundException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Class<?> c$protocolData = Class.forName("net.md_5.bungee.protocol.Protocol$ProtocolData");
        Field f$packetMap = c$protocolData.getDeclaredField("packetMap");// Supplier<? extends DefinedPacket>[]
        accessSet(f$packetMap);

        Method m$put = TObjectIntMap.class.getDeclaredMethod("put", Object.class, int.class);

        // 遍历所有版本协议
        for (Object data : getProtocolDataList(protocol, getClientSideField())) {//ProtocolData
            TObjectIntMap<?> packetMap = (TObjectIntMap<?>) f$packetMap.get(data);
            if (!packetMap.containsKey(originalClass)) continue;
            m$put.invoke(packetMap, redirectClass, packetMap.get(originalClass));
        }
    }

    private void accessSet(Field... fields) {
        Arrays.stream(fields).forEach(field -> field.setAccessible(true));
    }

    private Object[] getProtocolDataList(Protocol protocol, Field sideField) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        Class<?> c$directionData = Class.forName("net.md_5.bungee.protocol.Protocol$DirectionData");
        Field f$protocols = c$directionData.getDeclaredField("protocols");// TIntObjectMap<ProtocolData>
        accessSet(f$protocols, sideField);
        Object directionData = sideField.get(protocol);
        TIntObjectMap<?> protocols = (TIntObjectMap<?>) f$protocols.get(directionData); // ? is ProtocolData
        return protocols.values();
    }

    private Field getServerSideField() throws NoSuchFieldException {
        return Protocol.class.getDeclaredField("TO_SERVER");// DirectionData
    }

    private Field getClientSideField() throws NoSuchFieldException {
        return Protocol.class.getDeclaredField("TO_CLIENT");// DirectionData
    }

}
