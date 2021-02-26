package moe.caa.multilogin.core;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

public class ReflectUtil {

    public static Field getField(Class<?> clazz, Class<?> target){
        for(Field field : clazz.getDeclaredFields()){
            if(field.getType() == target){
                field.setAccessible(true);
                return field;
            }
        }
        throw new IllegalArgumentException(clazz + ": " + target);
    }

    public static Field getField(Class<?> clazz, String target) throws NoSuchFieldException {
        Field field = clazz.getDeclaredField(target);
        field.setAccessible(true);
        return field;
    }

    public static Method getMethod(Class<?> clazz, String name, Class<?>... args){
        for(Method method : clazz.getDeclaredMethods()){
            if(method.getName().equalsIgnoreCase(name)){
                if(Arrays.equals(method.getParameterTypes(), args)){
                    method.setAccessible(true);
                    return method;
                }
            }
        }
        throw new IllegalArgumentException(name);
    }

    public static Object getEnumIns(Class<?> clazz, String name) {
        for (Object constant : clazz.getEnumConstants()) {
            if(constant.toString().equalsIgnoreCase(name)){
                return constant;
            }
        }
        throw new IllegalArgumentException(name);
    }
}
