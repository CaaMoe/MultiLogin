package moe.caa.multilogin.bungee.injector;

import gnu.trove.map.TIntObjectMap;
import moe.caa.multilogin.api.injector.Injector;
import moe.caa.multilogin.api.main.MultiCoreAPI;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.ProtocolConstants;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

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
    private synchronized <T> void redirect() throws NoSuchFieldException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, ClassNotFoundException {
        Class<?> protocol$DirectionData = Class.forName("net.md_5.bungee.protocol.Protocol$DirectionData");
        Class<?> protocol$ProtocolData = Class.forName("net.md_5.bungee.protocol.Protocol$ProtocolData");

        // TIntObjectMap<ProtocolData>
        Field directionData$protocolsField = protocol$DirectionData.getDeclaredField("protocols");
        // DirectionData
        Field protocol$TO_SERVERField = Protocol.class.getDeclaredField("TO_SERVER");
        // Supplier<? extends DefinedPacket>[]
        Field protocolData$packetConstructorsField = protocol$ProtocolData.getDeclaredField("packetConstructors");

        directionData$protocolsField.setAccessible(true);
        protocol$TO_SERVERField.setAccessible(true);
        protocolData$packetConstructorsField.setAccessible(true);

        Object toServer = protocol$TO_SERVERField.get(Protocol.LOGIN);
        TIntObjectMap<?> protocols = (TIntObjectMap<?>) directionData$protocolsField.get(toServer);

        // 遍历所有支持版本
        for (int supportId : ProtocolConstants.SUPPORTED_VERSION_IDS) {
            DefinedPacket[] definedPackets = (DefinedPacket[]) protocols.get(supportId);
        }
    }
}
