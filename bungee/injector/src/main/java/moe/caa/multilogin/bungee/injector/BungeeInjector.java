package moe.caa.multilogin.bungee.injector;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TObjectIntMap;
import moe.caa.multilogin.api.injector.Injector;
import moe.caa.multilogin.api.main.MultiCoreAPI;
import moe.caa.multilogin.api.util.reflect.ReflectUtil;
import moe.caa.multilogin.bungee.injector.handler.AbstractMultiInitialHandler;
import moe.caa.multilogin.bungee.injector.redirect.MultiEncryptionResponse;
import moe.caa.multilogin.bungee.injector.redirect.MultiLoginRequest;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.packet.EncryptionResponse;
import net.md_5.bungee.protocol.packet.LoginRequest;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Supplier;

/**
 * Bungee 的注入器
 */
public class BungeeInjector implements Injector {
    @Override
    public void inject(MultiCoreAPI api) throws Throwable {
        AbstractMultiInitialHandler.init();

        redirectIn(Protocol.LOGIN, EncryptionResponse.class, () -> new MultiEncryptionResponse(api));
        redirectIn(Protocol.LOGIN, LoginRequest.class, () -> new MultiLoginRequest(api));
    }


    /**
     * 重定向输出
     *
     * @param stage            进行中的阶段
     * @param originalClass    原始类
     * @param redirectSupplier 重定向的Supplier
     * @param <T>              原始类的类型
     */
    private <T> void redirectIn(Protocol stage, Class<T> originalClass, Supplier<? extends T> redirectSupplier) throws NoSuchFieldException, ClassNotFoundException, IllegalAccessException {
        Class<?> c$protocolData = Class.forName("net.md_5.bungee.protocol.Protocol$ProtocolData");
        Field f$packetConstructors = c$protocolData.getDeclaredField("packetConstructors");// Supplier<? extends DefinedPacket>[]
        ReflectUtil.handleAccessible(f$packetConstructors);

        // 遍历所有版本协议
        for (Object data : getProtocolDataList(stage, true)) {//ProtocolData
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


    /**
     * 重定向输出
     *
     * @param stage         进行中的阶段
     * @param originalClass 原始类
     * @param redirectClass 重定向的类
     * @param <T>           原始类的类型
     */
    private <T> void redirectOut(Protocol stage, Class<T> originalClass, Class<? extends T> redirectClass) throws NoSuchFieldException, ClassNotFoundException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Class<?> c$protocolData = Class.forName("net.md_5.bungee.protocol.Protocol$ProtocolData");
        Field f$packetMap = c$protocolData.getDeclaredField("packetMap");// Supplier<? extends DefinedPacket>[]
        ReflectUtil.handleAccessible(f$packetMap);

        Method m$put = TObjectIntMap.class.getDeclaredMethod("put", Object.class, int.class);

        // 遍历所有版本协议
        for (Object data : getProtocolDataList(stage, false)) {//ProtocolData
            TObjectIntMap<?> packetMap = (TObjectIntMap<?>) f$packetMap.get(data);
            if (!packetMap.containsKey(originalClass)) continue;
            m$put.invoke(packetMap, redirectClass, packetMap.get(originalClass));
        }
    }

    /**
     * 获取不同方向下的不同阶段的全部协议
     *
     * @param stage    进行中的阶段
     * @param toServer 是否是发给服务端方向
     * @return 全部版本的协议
     */
    private Object[] getProtocolDataList(Protocol stage, boolean toServer) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        Class<?> c$directionData = Class.forName("net.md_5.bungee.protocol.Protocol$DirectionData");
        Field f$protocols = c$directionData.getDeclaredField("protocols");// TIntObjectMap<ProtocolData>
        Field sideField = toServer ? Protocol.class.getDeclaredField("TO_SERVER") : Protocol.class.getDeclaredField("TO_CLIENT");
        ReflectUtil.handleAccessible(f$protocols);
        ReflectUtil.handleAccessible(sideField);
        Object directionData = sideField.get(stage);
        TIntObjectMap<?> protocols = (TIntObjectMap<?>) f$protocols.get(directionData); // ? is ProtocolData
        return protocols.values();
    }
}
