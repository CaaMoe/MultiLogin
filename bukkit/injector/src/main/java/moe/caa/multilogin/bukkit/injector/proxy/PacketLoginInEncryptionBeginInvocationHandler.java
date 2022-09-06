package moe.caa.multilogin.bukkit.injector.proxy;

import com.mojang.authlib.GameProfile;
import moe.caa.multilogin.api.util.reflect.ReflectUtil;
import moe.caa.multilogin.bukkit.injector.BukkitInjector;
import moe.caa.multilogin.bukkit.injector.Contents;
import moe.caa.multilogin.bukkit.injector.LoginListenerSynchronizer;
import moe.caa.multilogin.bukkit.main.MultiLoginBukkit;
import moe.caa.multilogin.core.proxy.InterceptMethodInvocationHandler;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.*;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;
import java.util.UUID;
import java.util.concurrent.Callable;

public class PacketLoginInEncryptionBeginInvocationHandler extends InterceptMethodInvocationHandler {
    private static Class<?> proxyClass;
    private static MethodHandle multilgoin_original_handlerFieldSetter;
    private static MethodHandle multilgoin_original_handlerFieldGetter;
    private static MethodHandle gameProfileGetter;
    private static MethodHandle chatComponentConstructor;

    public PacketLoginInEncryptionBeginInvocationHandler() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        this(BukkitInjector.getPacketLoginInEncryptionBeginClass().getConstructor().newInstance());
    }

    /**
     * @param handle 被代理的类
     */
    public PacketLoginInEncryptionBeginInvocationHandler(Object handle) {
        super(handle, m -> m.getParameterTypes().length == 1 && m.getParameterTypes()[0].isAssignableFrom(BukkitInjector.getPacketListenerClass()), ((method, objects) -> {
            try {
                Object proxyObj = newFakeProxyLoginListener(objects[0], proxyClass);
                objects[0] = proxyObj;
                return method.invoke(handle, objects);
            } catch (Throwable e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }));
    }

    public static void init() throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, ClassNotFoundException {
        String name = "moe.caa.multilogin.bukkit.injector.LoginListenerProxy$" + UUID.randomUUID().toString().substring(0, 6);
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        gameProfileGetter = lookup.unreflectGetter(ReflectUtil.handleAccessible(ReflectUtil.findNoStaticField(BukkitInjector.getLoginListenerClass(), GameProfile.class)));
        String original_handler = "multilgoin_original_handler";
        proxyClass = new ByteBuddy()
                .subclass(BukkitInjector.getLoginListenerClass())
                .name(name)
                .defineField(original_handler, BukkitInjector.getLoginListenerClass(), Visibility.PUBLIC)
                .method(ElementMatchers.takesArguments(1)
                        .and(ElementMatchers.takesArguments(String.class)
                                .or(ElementMatchers.takesArguments(BukkitInjector.getIChatBaseComponentClass())
                                        .or(ElementMatchers.takesArguments(BukkitInjector.getPacketLoginInEncryptionBeginClass()))
                                )
                        )).intercept(
                        MethodDelegation.to(ProxyMethodInterceptor.class)
                )
                .make()
                .load(((MultiLoginBukkit) BukkitInjector.getApi().getPlugin()).getMlPluginLoader()
                        .getPluginClassLoader().self(), ClassLoadingStrategy.Default.WRAPPER)
                .getLoaded();

        Field field = proxyClass.getField(original_handler);

        multilgoin_original_handlerFieldSetter = lookup.unreflectSetter(field);
        multilgoin_original_handlerFieldGetter = lookup.unreflectGetter(field);
        chatComponentConstructor = lookup.unreflectConstructor(ReflectUtil.handleAccessible(BukkitInjector.getChatComponentTextClass().getConstructor(String.class)));
    }

    private static Object newFakeProxyLoginListener(Object source, Class<?> byteBuddyProxyClass) throws Throwable {
        Constructor<?> constructor = byteBuddyProxyClass.getDeclaredConstructors()[0];
        Object[] parameters = new Object[constructor.getParameterCount()];
        for (int i = 0; i < constructor.getParameterTypes().length; i++) {
            parameters[i] = ReflectUtil.handleAccessible(ReflectUtil.findNoStaticField(BukkitInjector.getLoginListenerClass(), constructor.getParameterTypes()[i])).get(source);
        }
        Object o = constructor.newInstance(parameters);
        // 绑定 原来的 handler
        multilgoin_original_handlerFieldSetter.invoke(o, source);
        return o;
    }

    public static <T> void apply(Class<?> mb, T source, T target) throws IllegalAccessException {
        for (Field declaredField : mb.getDeclaredFields()) {
            if (Modifier.isStatic(declaredField.getModifiers())) continue;
            Field field = ReflectUtil.handleAccessible(declaredField);
            field.set(target, field.get(source));
        }
        for (Field declaredField : mb.getFields()) {
            if (Modifier.isStatic(declaredField.getModifiers())) continue;
            Field field = ReflectUtil.handleAccessible(declaredField);
            field.set(target, field.get(source));
        }
    }


    public static class ProxyMethodInterceptor {
        @RuntimeType
        public static Object intercept(
                @SuperCall Callable<Object> supercall,
                @This Object proxyObj,
                @Origin Method method,
                @AllArguments Object[] args) throws Throwable {

            Object origin = multilgoin_original_handlerFieldGetter.invoke(proxyObj);
            // 调用前同步数据
            apply(BukkitInjector.getLoginListenerClass(), origin, proxyObj);

            Object arg = args[0];
            Class<?> argType = arg.getClass();
            if (!argType.equals(BukkitInjector.getPacketLoginInEncryptionBeginClass())) {
                if (argType.equals(String.class) || BukkitInjector.getIChatBaseComponentClass().isAssignableFrom(argType)) {
                    GameProfile profile = (GameProfile) gameProfileGetter.invoke(proxyObj);
                    Contents.KickMessageEntry remove = Contents.getKickMessageEntryMap().remove(profile.getName());
                    if (remove != null) {
                        if (arg.getClass().equals(String.class)) {
                            arg = remove.getKickMessage();
                        } else {
                            arg = chatComponentConstructor.invoke(remove.getKickMessage());
                        }
                    }
                    args[0] = arg;
                    return method.invoke(origin, args);
                }
            }
            Object invoke = supercall.call();
            // 执行后跟进数据
            apply(BukkitInjector.getLoginListenerClass(), proxyObj, origin);

            if (argType.equals(BukkitInjector.getPacketLoginInEncryptionBeginClass())) {
                LoginListenerSynchronizer.getInstance().putEntry(proxyObj, origin);
            }
            return invoke;
        }
    }
}
