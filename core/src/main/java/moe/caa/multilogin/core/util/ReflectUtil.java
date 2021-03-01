package moe.caa.multilogin.core.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * 反射工具类
 */
public class ReflectUtil {
    private static MethodHandles.Lookup lookup = MethodHandles.lookup();

    /**
     * 通过class对象在指定的class类中检索第一次出现的Field
     *
     * @param clazz  指定的class
     * @param target field的class类型
     * @return 检索到的第一个field
     */
    public static Field getField(Class<?> clazz, Class<?> target) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getType() != target) continue;
            field.setAccessible(true);
            return field;
        }
        throw new IllegalArgumentException(clazz + ": " + target);
    }

    /**
     * 通过field名在指定的class类中检索Field
     *
     * @param clazz  指定的class
     * @param target field的name
     * @return 检索到的field
     */
    public static Field getField(Class<?> clazz, String target) throws NoSuchFieldException {
        Field field = clazz.getDeclaredField(target);
        field.setAccessible(true);
        return field;
    }

    /**
     * 通过class数组对象在指定的class类中检索method
     *
     * @param clazz 指定的class
     * @param name  method的name
     * @param args  参数数组
     * @return 检索到的method
     */
    public static Method getMethod(Class<?> clazz, String name, Class<?>... args) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (!method.getName().equalsIgnoreCase(name)) continue;
            if (!Arrays.equals(method.getParameterTypes(), args)) continue;
            method.setAccessible(true);
            return method;
        }
        throw new IllegalArgumentException(name);
    }

    /**
     * 通过enum 名字检索指定的class（enum）中的enum
     *
     * @param clazz 指定的class
     * @param name  enum的name
     * @return enum
     */
    public static Object getEnumIns(Class<?> clazz, String name) {
        for (Object constant : clazz.getEnumConstants()) {
            if (!constant.toString().equalsIgnoreCase(name)) continue;
            return constant;
        }
        throw new IllegalArgumentException(name);
    }

    /**
     * 通过field名在指定的class类中检索FieldSetter
     *
     * @param clazz  指定的class
     * @param target field的class类型
     * @return 检索到的fieldSetter
     */
    public static MethodHandle getFieldUnReflectSetter(Class<?> clazz, Class<?> target) throws IllegalAccessException {
        return lookup.unreflectSetter(getField(clazz, target));
    }

    /**
     * 通过field名在指定的class类中检索FieldSetter
     *
     * @param clazz  指定的class
     * @param target field的name
     * @return 检索到的fieldSetter
     */
    public static MethodHandle getFieldUnReflectSetter(Class<?> clazz, String target) throws NoSuchFieldException, IllegalAccessException {
        return lookup.unreflectSetter(getField(clazz, target));
    }

    /**
     * 通过field名在指定的class类中检索FieldSetter
     *
     * @param clazz  指定的class
     * @param target field的class类型
     * @return 检索到的fieldGetter
     */
    public static MethodHandle getFieldUnReflectGetter(Class<?> clazz, Class<?> target) throws IllegalAccessException {
        return lookup.unreflectGetter(getField(clazz, target));
    }

    /**
     * 通过field名在指定的class类中检索FieldSetter
     *
     * @param clazz  指定的class
     * @param target field的name
     * @return 检索到的fieldGetter
     */
    public static MethodHandle getFieldUnReflectGetter(Class<?> clazz, String target) throws NoSuchFieldException, IllegalAccessException {
        return lookup.unreflectGetter(getField(clazz, target));
    }
}
