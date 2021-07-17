package moe.caa.multilogin.core.util;

import moe.caa.multilogin.core.exception.NoSuchEnumException;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class ReflectUtil {
    public static MethodHandles.Lookup super_lookup;

    public static void init() throws ClassNotFoundException, IllegalAccessException, InvocationTargetException, NoSuchFieldException, NoSuchMethodException {
        Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
        Field theUnsafeField = getField(unsafeClass, unsafeClass, true);
        Method theUnsafeGetObjectMethod = getMethod(unsafeClass, "getObject", false, Object.class, long.class);
        Method theUnsafeStaticFieldOffsetMethod = getMethod(unsafeClass, "staticFieldOffset", false, Field.class);
        Object theUnsafe = theUnsafeField.get(null);
        Field implLookup = getField(MethodHandles.Lookup.class, "IMPL_LOOKUP", false);

        super_lookup = (MethodHandles.Lookup) theUnsafeGetObjectMethod.invoke(theUnsafe, MethodHandles.Lookup.class, theUnsafeStaticFieldOffsetMethod.invoke(theUnsafe, implLookup));
    }

    /**
     * 通过 Class 对象在指定的 Class 类中检索第一次出现的 Field
     *
     * @param clazz            指定的 Class
     * @param target           Field 的 Class类型
     * @param handleAccessible 是否操作检索到的 Field 可见
     * @return 检索到的第一个 Field
     */
    public static Field getField(Class<?> clazz, Class<?> target, boolean handleAccessible) throws NoSuchFieldException {
        return getField0(clazz, clazz, target, handleAccessible);
    }

    private static Field getField0(Class<?> source, Class<?> clazz, Class<?> target, boolean handleAccessible) throws NoSuchFieldException {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getType() != target) continue;
            if (handleAccessible) field.setAccessible(true);
            return field;
        }
        clazz = clazz.getSuperclass();
        if (clazz != null) return getField(clazz, target, handleAccessible);
        throw new NoSuchFieldException(target.getName() + " type in " + source.getName());
    }

    /**
     * 通过 Field 名在指定的 Class 类中检索 Field
     *
     * @param clazz            指定的 Class
     * @param target           Field 的 name
     * @param handleAccessible 是否操作检索到的 Field 可见
     * @return 检索到的 Field
     */
    public static Field getField(Class<?> clazz, String target, boolean handleAccessible) throws NoSuchFieldException {
        try {
            Field field;
            field = clazz.getDeclaredField(target);
            if (handleAccessible) field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            throw new NoSuchFieldException(target + " field in " + clazz.getName());
        }
    }

    /**
     * 通过 Class 数组对象在指定的 Class 类中检索 Method
     *
     * @param clazz            指定的 Class
     * @param name             Method 的 name
     * @param handleAccessible 是否操作检索到的 Field 可见
     * @param args             参数数组
     * @return 检索到的 Method
     */
    public static Method getMethod(Class<?> clazz, String name, boolean handleAccessible, Class<?>... args) throws NoSuchMethodException {
        for (Method method : clazz.getDeclaredMethods()) {
            if (!method.getName().equalsIgnoreCase(name)) continue;
            if (!Arrays.equals(method.getParameterTypes(), args)) continue;
            if (handleAccessible) method.setAccessible(true);
            return method;
        }
        throw new NoSuchMethodException(name + " method in " + clazz.getName());
    }

    /**
     * 通过 enum name检索指定的 Class（enum）中的实例
     *
     * @param clazz 指定的 Class
     * @param name  enum 的 name
     * @return 指定的实例
     */
    public static Object getEnumIns(Class<? extends Enum<?>> clazz, String name) throws NoSuchEnumException {
        for (Enum<?> constant : clazz.getEnumConstants()) {
            if (!constant.name().equalsIgnoreCase(name)) continue;
            return constant;
        }
        throw new NoSuchEnumException(name + " enum in " + clazz.getName());
    }

    /**
     * 判断是否可以转型
     *
     * @param src    源类型
     * @param parent 目标类型
     * @return 是否可转
     */
    public static boolean isCaseClass(Class<?> src, Class<?> parent) {
        if (parent == src) return true;
        for (Class<?> anInterface : src.getInterfaces()) {
            if (anInterface == parent) {
                return true;
            }
        }
        src = src.getSuperclass();
        if (src != null) return isCaseClass(src, parent);
        return false;
    }

    public static Class<?> getClass(String name) {
        try {
            return Class.forName(name);
        } catch (Exception e) {
            return null;
        }
    }
}
