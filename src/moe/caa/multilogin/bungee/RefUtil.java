package moe.caa.multilogin.bungee;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.connection.InitialHandler;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

public class RefUtil {
    private static final Class<PreLoginEvent> preLoginEventClass = PreLoginEvent.class;

    public static Field getField(Class clazz, Class target){
        for(Field field : clazz.getDeclaredFields()){
            if(field.getType() == target){
                field.setAccessible(true);
                return field;
            }
        }
        throw new IllegalArgumentException();
    }

    public static Field getField(Class clazz, String target) throws NoSuchFieldException {
        Field field = clazz.getDeclaredField(target);
        field.setAccessible(true);
        return field;
    }

    public static Method getMethod(Class clazz, String name, Class... args){
        for(Method method : clazz.getDeclaredMethods()){
            if(method.getName().equalsIgnoreCase(name)){
                if(Arrays.equals(method.getParameterTypes(), args)){
                    return method;
                }
            }
        }
        throw new IllegalArgumentException(name);
    }

    public static Object getEnumIns(Class clazz,String name) {
        for (Object constant : clazz.getEnumConstants()) {
            if(constant.toString().equalsIgnoreCase(name)){
                return constant;
            }
        }
        throw new IllegalArgumentException(name);
    }

    public static void modify(PreLoginEvent event) throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException {
        Field modTar = preLoginEventClass.getDeclaredField("connection");
        modTar.setAccessible(true);
        InitialHandler vanHandle = (InitialHandler) modTar.get(event);
        modTar.set(event, new MultiInitialHandler(BungeeCord.getInstance(),vanHandle.getListener(), vanHandle));
    }
}
