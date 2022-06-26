package moe.caa.multilogin.api.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectUtil {
    public static Method handleAccessible(Method method) {
        method.setAccessible(true);
        return method;
    }

    public static Constructor<?> handleAccessible(Constructor<?> constructor) {
        constructor.setAccessible(true);
        return constructor;
    }

    public static Field handleAccessible(Field field) {
        field.setAccessible(true);
        return field;
    }
}
