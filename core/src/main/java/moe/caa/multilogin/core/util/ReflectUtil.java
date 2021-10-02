package moe.caa.multilogin.core.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.var;
import moe.caa.multilogin.core.exception.NoSuchEnumException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * 反射工具类库
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReflectUtil {

    /**
     * 操作 Method accessible 属性
     *
     * @param method        指定 Method
     * @param newAccessible 新的 accessible flag
     * @return 指定的 Method 本身
     */
    public static Method handleAccessible(Method method, boolean newAccessible) {
        method.setAccessible(newAccessible);
        return method;
    }

    /**
     * 操作 Method accessible 属性
     *
     * @param field         指定 Field
     * @param newAccessible 新的 accessible flag
     * @return 指定的 Field 本身
     */
    public static Field handleAccessible(Field field, boolean newAccessible) {
        field.setAccessible(newAccessible);
        return field;
    }

    /**
     * 获取指定 Class 对象内匹配的第一个 Field
     *
     * @param targetClass 指定 Class
     * @param type        Field 的类型
     * @return 第一次匹配的 Field
     */
    public static Field getField(Class<?> targetClass, Type type) throws NoSuchFieldException {
        for (var field : targetClass.getDeclaredFields()) {
            if (field.getType() != type) continue;
            return field;
        }
        throw new NoSuchFieldException(type.getTypeName());
    }

    /**
     * 通过 enum name检索指定的 Class（enum）中的实例
     *
     * @param clazz 指定的 Class
     * @param name  enum 的 name
     * @return 指定的实例
     */
    public static Object getEnumIns(Class<? extends Enum<?>> clazz, String name) throws NoSuchEnumException {
        for (var constant : clazz.getEnumConstants()) {
            if (!constant.name().equalsIgnoreCase(name)) continue;
            return constant;
        }
        throw new NoSuchEnumException(name + " enum in " + clazz.getName());
    }

    /**
     * 判断类是否已被加载
     *
     * @param name 类全名
     * @return 是否已被加载
     */
    public static boolean isExistsClass(String name) {
        try {
            Class.forName(name);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
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
        for (var anInterface : src.getInterfaces()) {
            if (anInterface == parent) {
                return true;
            }
        }
        src = src.getSuperclass();
        if (src != null) return isCaseClass(src, parent);
        return false;
    }
}
