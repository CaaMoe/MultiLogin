package moe.caa.multilogin.bungee;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.HandlerBoss;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

public class RefUtil {
    private static final Class<PreLoginEvent> preLoginEventClass = PreLoginEvent.class;
    private static final Class<InitialHandler> initialHandlerClass = InitialHandler.class;

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
                    method.setAccessible(true);
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

    public static void modify(PreLoginEvent event) throws Exception {
        Field modTar = preLoginEventClass.getDeclaredField("connection");
        Field chField = getField(initialHandlerClass, ChannelWrapper.class);
        modTar.setAccessible(true);
        InitialHandler vanHandle = (InitialHandler) modTar.get(event);
        ChannelWrapper ch = (ChannelWrapper) chField.get(vanHandle);
        MultiInitialHandler mh = new MultiInitialHandler(BungeeCord.getInstance(),vanHandle.getListener(), vanHandle);
        mh.connected(ch);
        ch.getHandle().pipeline().get(HandlerBoss.class).setHandler(mh);
    }
}
