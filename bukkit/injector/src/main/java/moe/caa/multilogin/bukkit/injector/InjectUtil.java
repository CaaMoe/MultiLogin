package moe.caa.multilogin.bukkit.injector;

import com.google.common.collect.BiMap;
import moe.caa.multilogin.api.util.reflect.ReflectUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
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
        Field j = ReflectUtil.findNoStaticField(protocol.getDeclaringClass(), Map.class);
        Map<?, ?> packetHandlers = (Map<?, ?>) ReflectUtil.handleAccessible(j).get(protocol);

        Object packetHandler = packetHandlers.get(direction);
        // 如果没有，代表发包方向不一样
        if (packetHandler == null) {
            return false;
        }

        try {
            // List<Function<PacketByteBuf, ? extends Packet<T>>> packetFactories
            Field c = ReflectUtil.findNoStaticField(packetHandler.getClass(), List.class);
            List<?> packetFactories = (List<?>) ReflectUtil.handleAccessible(c).get(packetHandler);

            Method list$set = List.class.getDeclaredMethod("set", int.class, Object.class);
            list$set.invoke(
                    packetFactories, packetId, redirectFunction.apply(packetFactories.get(packetId))
            );
            return true;
        } catch (Throwable throwable) {
            // BiMap<Integer, Class<? extends Packet>> ?
            // BiMap<Class<? extends Packet>, Integer> ?
            Field c = ReflectUtil.findNoStaticField(packetHandler.getClass(), BiMap.class);
            BiMap<?, ?> biMap = (BiMap<?, ?>) ReflectUtil.handleAccessible(c).get(packetHandler);
            Method map$put = Map.class.getDeclaredMethod("put", Object.class, Object.class);
            if (biMap.entrySet().iterator().next().getKey().getClass() == int.class) {
                map$put.invoke(
                        biMap, packetId, redirectFunction.apply(biMap.get(packetId))
                );
            } else {
                AtomicReference<Object> origin = new AtomicReference<>();
                biMap.entrySet().removeIf(entry -> {
                    if ((int) entry.getValue() == packetId) {
                        origin.set(entry.getKey());
                        return true;
                    }
                    return false;
                });
                map$put.invoke(
                        biMap, redirectFunction.apply(origin.get()), packetId
                );
            }

            return true;
        }
    }

    /**
     * 查找 NMS 类
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

    /**
     * 查找 OBC 类
     *
     * @param baseName   类名
     * @param path       新包名
     * @param nmsVersion nms 版本
     */
    public static Class<?> findOBCClass(String baseName, String path, String nmsVersion) throws ClassNotFoundException {
        path += path.trim().length() == 0 ? "" : ".";
        String className = "org.bukkit.craftbukkit." + nmsVersion + "." + path + baseName;
        return Class.forName(className);
    }

    /**
     * 暴力同步类型相同的方法的字段
     */
    public static <T> void apply(Class<?> mb, T source, T target) throws IllegalAccessException {
        for (Field declaredField : mb.getDeclaredFields()) {
            if (Modifier.isStatic(declaredField.getModifiers())) continue;
            Field field = ReflectUtil.handleAccessible(declaredField);
            field.set(target, field.get(source));
        }
        for (Field declaredField : mb.getFields()) {
            if (Modifier.isStatic(declaredField.getModifiers())) continue;
            Field field = ReflectUtil.handleAccessible(declaredField);
            field.set(target, field.get(source));
        }
    }
}
