package moe.caa.multilogin.bukkit.injector;

import moe.caa.multilogin.api.util.reflect.ReflectUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Bukkit 注入专用工具库
 */
public class InjectUtil {

    /**
     * 重定向数据包
     *
     * @param protocol         协议阶段
     * @param direction        数据包方向
     * @param packetId         数据包 ID
     * @param redirectFunction 重定向函数，输入重定向前的值并返回重定向后的结果
     */
    public static boolean redirectInput(Enum<?> protocol, Enum<?> direction, int packetId, Function<Object, Object> redirectFunction) throws Throwable {
        // Map<NetworkSide, ? extends PacketHandler<?>>;
        Field j = ReflectUtil.findNoStaticField(protocol.getClass(), Map.class);
        Map<?, ?> packetHandlers = (Map<?, ?>) ReflectUtil.handleAccessible(j).get(protocol);

        Object packetHandler = packetHandlers.get(direction);
        // 如果没有，代表发包方向不一样
        if (packetHandler == null) {
            return false;
        }

        // List<Function<PacketByteBuf, ? extends Packet<T>>> packetFactories
        Field c = ReflectUtil.findNoStaticField(packetHandler.getClass(), List.class);
        List<?> packetFactories = (List<?>) ReflectUtil.handleAccessible(c).get(packetHandler);

        Method list$set = List.class.getDeclaredMethod("set", int.class, Object.class);
        list$set.invoke(
                packetFactories, packetId, redirectFunction.apply(packetFactories.get(packetId))
        );
        return true;
    }

    /**
     * 查找 NMS 方法
     *
     * @param baseName   类名
     * @param path       新包名
     * @param nmsVersion nms 版本
     */
    public static Class<?> findNMSClass(String baseName, String path, String nmsVersion) throws ClassNotFoundException {
        try {
            return Class.forName("net.minecraft.server." + nmsVersion + "." + baseName);
        } catch (Throwable e) {
            try {
                return Class.forName("net.minecraft." + path + "." + baseName);
            } catch (Throwable ex) {
                throw new ClassNotFoundException(baseName, ex);
            }
        }
    }
}
