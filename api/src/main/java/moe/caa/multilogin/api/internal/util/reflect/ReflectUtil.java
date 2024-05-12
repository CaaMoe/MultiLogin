package moe.caa.multilogin.api.internal.util.reflect;

import org.jetbrains.annotations.ApiStatus;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.function.Function;

/**
 * 反射工具库
 */
@ApiStatus.Internal
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
    public static<T> Constructor<T> handleAccessible(Constructor<T> constructor) {
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
        for (Field field : target.getFields()) {
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

    public static Method findStaticMethodByReturnTypeAndParameters(Class<?> target, Type returnType, Type... fieldTypes) throws NoSuchMethodException {
        for (Method method : target.getDeclaredMethods()) {
            if (!Modifier.isStatic(method.getModifiers())) continue;
            if (Arrays.equals(method.getParameterTypes(), fieldTypes)) {
                if (returnType.equals(method.getReturnType())) {
                    return method;
                }
            }
        }
        throw new NoSuchMethodException(target.getName() + " Types: " + Arrays.toString(fieldTypes));
    }

    public static Method findNoStaticMethodByReturnType(Class<?> target, Type returnType) throws NoSuchMethodException {
        for (Method method : target.getDeclaredMethods()) {
            if (!Modifier.isStatic(method.getModifiers())) continue;
            if (method.getReturnType().equals(returnType)) {
                return method;
            }
        }
        throw new NoSuchMethodException(target.getName() + " Types: " + returnType);
    }

    /**
     * 修改一个 Record 对象的一个字段
     *
     * @param source   record 对象
     * @param match    对象匹配函数
     * @param redirect 重定向对象函数
     * @return 修改后的 Record，源 Record 不变
     */
    public static Object redirectRecordObject(Object source, Function<Object, Boolean> match, Function<Object, Object> redirect)
            throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {
        LinkedHashMap<Field, Object> fieldObjectMap = new LinkedHashMap<>();
        for (Field field : source.getClass().getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            Object value = ReflectUtil.handleAccessible(field).get(source);
            if (match.apply(value)) {
                value = redirect.apply(value);
            }
            fieldObjectMap.put(field, value);
        }

        final Constructor<?> declaredConstructor = source.getClass().getDeclaredConstructor(
                fieldObjectMap.keySet().stream().map(Field::getType).toArray(Class[]::new)
        );

        return declaredConstructor.newInstance(fieldObjectMap.values().toArray());
    }
}
