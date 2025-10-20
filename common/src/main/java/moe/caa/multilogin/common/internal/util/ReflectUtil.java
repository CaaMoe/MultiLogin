package moe.caa.multilogin.common.internal.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectUtil {
    public static Method openAccess(Method method) {
        method.setAccessible(true);
        return method;
    }

    public static Field openAccess(Field field) {
        field.setAccessible(true);
        return field;
    }
}
