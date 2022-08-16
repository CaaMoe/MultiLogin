package moe.caa.multilogin.api.util;

import java.lang.reflect.*;
import java.util.Arrays;

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

    /**
     * 指定一个目标类检索第一个出现的成员 Field
     */
    public static Field findNoStaticField(Class<?> target, Type fieldType) throws NoSuchFieldException {
        for (Field field : target.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) continue;
            if (field.getType() == fieldType) {
                return field;
            }
        }
        throw new NoSuchFieldException("Type: " + fieldType.getTypeName());
    }

    /**
     * 指定一个目标类检索第一个出现的成员 Method
     */
    public static Method findNoStaticMethodByParameters(Class<?> target, Type... fieldTypes) throws NoSuchMethodException {
        for (Method method : target.getDeclaredMethods()) {
            if (Modifier.isStatic(method.getModifiers())) continue;
            if (Arrays.equals(method.getParameterTypes(), fieldTypes)) {
                return method;
            }
        }
        throw new NoSuchMethodException(target.getName() + " Types: " + Arrays.toString(fieldTypes));
    }

    /**
     * 指定一个目标类检索第一个出现的静态 Method
     */
    public static Method findStaticMethodByParameters(Class<?> target, Type... fieldTypes) throws NoSuchMethodException {
        for (Method method : target.getDeclaredMethods()) {
            if (!Modifier.isStatic(method.getModifiers())) continue;
            if (Arrays.equals(method.getParameterTypes(), fieldTypes)) {
                return method;
            }
        }
        throw new NoSuchMethodException(target.getName() + " Types: " + Arrays.toString(fieldTypes));
    }
}
