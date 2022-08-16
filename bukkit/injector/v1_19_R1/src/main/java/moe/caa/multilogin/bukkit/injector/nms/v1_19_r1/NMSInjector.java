package moe.caa.multilogin.bukkit.injector.nms.v1_19_r1;

import moe.caa.multilogin.api.injector.Injector;
import moe.caa.multilogin.api.main.MultiCoreAPI;
import moe.caa.multilogin.api.util.ReflectUtil;
import moe.caa.multilogin.bukkit.injector.nms.v1_19_r1.handler.MultiPacketLoginInEncryptionBeginHandler;
import moe.caa.multilogin.bukkit.injector.nms.v1_19_r1.redirect.MultiPacketLoginInEncryptionBegin;
import net.minecraft.network.EnumProtocol;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.protocol.EnumProtocolDirection;
import net.minecraft.network.protocol.Packet;

import java.lang.reflect.Field;
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
        redirectInput(EnumProtocol.d, EnumProtocolDirection.a, 0x01, var0 -> new MultiPacketLoginInEncryptionBegin(var0, api));
    }

    /**
     * 重定向数据包
     * @param bound 协议阶段
     * @param direction 数据包方向
     * @param packetId 数据包 ID
     * @param functionRedirect 重定向后的 Function
     */
    private  void redirectInput(EnumProtocol bound, EnumProtocolDirection direction, int packetId, Function<PacketDataSerializer, ? extends Packet<?>> functionRedirect) throws Throwable {
        // Map<NetworkSide, ? extends PacketHandler<?>>;
        Field j = ReflectUtil.findField(bound.getClass(), Map.class);
        Map<?, ?> packetHandlers = (Map<?, ?>) ReflectUtil.handleAccessible(j).get(bound);

        Object packetHandler = packetHandlers.get(direction);
        // 如果没有，代表发包方向不一样
        if(packetHandler == null){
            return;
        }

        // List<Function<PacketByteBuf, ? extends Packet<T>>> packetFactories
        Field c = ReflectUtil.findField(packetHandler.getClass(), List.class);
        List<?> packetFactories = (List<?>) ReflectUtil.handleAccessible(c).get(packetHandler);

        List.class.getDeclaredMethod("set", int.class, Object.class).invoke(
                packetFactories, packetId, functionRedirect
        );
    }
}
