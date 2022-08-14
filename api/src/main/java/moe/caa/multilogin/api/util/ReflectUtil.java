package moe.caa.multilogin.api.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 反射工具库
 */
public class ReflectUtil {

    /**
     * 操作 Accessible 属性
     */
    public static Method handleAccessible(Method method) {
        method.setAccessible(true);
        return method;
    }

    /**
     * 操作 Accessible 属性
     */
    public static Constructor<?> handleAccessible(Constructor<?> constructor) {
        constructor.setAccessible(true);
        return constructor;
    }

    /**
     * 操作 Accessible 属性
     */
    public static Field handleAccessible(Field field) {
        field.setAccessible(true);
        return field;
    }
}
