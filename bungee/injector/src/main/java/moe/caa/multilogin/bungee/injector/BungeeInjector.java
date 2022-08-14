package moe.caa.multilogin.bungee.injector;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TObjectIntMap;
import moe.caa.multilogin.api.injector.Injector;
import moe.caa.multilogin.api.main.MultiCoreAPI;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.ProtocolConstants;

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
        throw new RuntimeException("Unsupported Bungee Server.");
    }

    /**
     * 重定向包，未完成
     */
    private synchronized <T> void redirect(Protocol protocol, boolean toServer, Class<T> target, Class<? extends T> redirect, Supplier<? extends T> supplierRedirect) throws NoSuchFieldException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, ClassNotFoundException {
        Class<?> protocol$DirectionData = Class.forName("net.md_5.bungee.protocol.Protocol$DirectionData");
        Class<?> protocol$ProtocolData = Class.forName("net.md_5.bungee.protocol.Protocol$ProtocolData");

        // TIntObjectMap<ProtocolData>
        Field directionData$protocolsField = protocol$DirectionData.getDeclaredField("protocols");
        // DirectionData
        Field protocol$TO_SERVERField = Protocol.class.getDeclaredField("TO_SERVER");
        Field protocol$TO_CLIENTField = Protocol.class.getDeclaredField("TO_CLIENT");
        // Supplier<? extends DefinedPacket>[]
        Field protocolData$packetConstructorsField = protocol$ProtocolData.getDeclaredField("packetConstructors");
        //TObjectIntMap<Class<? extends DefinedPacket>>
        Field protocolData$packetMapField = protocol$ProtocolData.getDeclaredField("packetMap");

        Method tObjectIntMap$putMethod = TObjectIntMap.class.getDeclaredMethod("put", Object.class, int.class);

        directionData$protocolsField.setAccessible(true);
        protocol$TO_SERVERField.setAccessible(true);
        protocolData$packetConstructorsField.setAccessible(true);
        protocolData$packetMapField.setAccessible(true);

        Object toTarget = toServer ? protocol$TO_SERVERField.get(protocol) : protocol$TO_CLIENTField.get(protocol);
        // TIntObjectMap<ProtocolData>
        TIntObjectMap<?> protocols = (TIntObjectMap<?>) directionData$protocolsField.get(toTarget);

        // 遍历所有支持版本
        for (int supportId : ProtocolConstants.SUPPORTED_VERSION_IDS) {
            // ProtocolData
            Object data = protocols.get(supportId);

            // 替换包
            // Supplier<? extends DefinedPacket>[]
            Supplier<?>[] definedPackets = (Supplier<?>[]) (protocolData$packetConstructorsField.get(data));
            for (int i = 0; i < definedPackets.length; i++) {
                if(definedPackets[i].get().getClass().equals(target)){
                    definedPackets[i] = supplierRedirect;
                }
            }

            // 替换出口方向包
            TObjectIntMap<?> intMap = (TObjectIntMap<?>) protocolData$packetMapField.get(data);
            tObjectIntMap$putMethod.invoke(
                    intMap, redirect, intMap.get(target)
            );
        }
    }
}
