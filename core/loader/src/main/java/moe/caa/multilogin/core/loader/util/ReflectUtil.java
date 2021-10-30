package moe.caa.multilogin.core.loader.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.Method;
import java.util.Arrays;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReflectUtil {

    public static Method getMethodWithParent(Class<?> clazz, String name, boolean handleAccessible, Class<?>... args) throws NoSuchMethodException {
        for (Method method : clazz.getDeclaredMethods()) {
            if (!method.getName().equalsIgnoreCase(name)) continue;
            if (!Arrays.equals(method.getParameterTypes(), args)) continue;
            if (handleAccessible) method.setAccessible(true);
            return method;
        }
        if (clazz != Object.class)
            return getMethodWithParent(clazz.getSuperclass(), name, handleAccessible, args);
        throw new NoSuchMethodException(name + " method in " + clazz.getName());
    }
}
