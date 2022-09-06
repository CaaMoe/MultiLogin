package moe.caa.multilogin.bukkit.injector.subclasshandler;

import com.mojang.authlib.GameProfile;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import moe.caa.multilogin.api.util.reflect.ReflectUtil;
import moe.caa.multilogin.bukkit.injector.BukkitInjector;
import moe.caa.multilogin.bukkit.main.MultiLoginBukkit;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.implementation.bind.annotation.This;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.Callable;

public class LoginListenerSubclassHandler {
    private final BukkitInjector injector;
    @Getter
    private Class<?> proxyLoginListenerClass;
    private MethodHandle multilogin_original_handlerFieldSetter;
    private MethodHandle multilogin_original_handlerFieldGetter;
    private MethodHandle gameProfileGetter;
    private MethodHandle chatComponentConstructor;

    public LoginListenerSubclassHandler(BukkitInjector injector) {
        this.injector = injector;
    }

    public void init() throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException {
        String name = "moe.caa.multilogin.bukkit.injector.LoginListenerProxy$" + UUID.randomUUID().toString().substring(0, 6).toUpperCase(Locale.ROOT);
        String original_handler = "multilgoin_original_handler";
        proxyLoginListenerClass = new ByteBuddy()
                .subclass(injector.getLoginListenerClass())
                .name(name)
                .defineField(original_handler, injector.getLoginListenerClass(), Visibility.PUBLIC)
                .method(ElementMatchers.takesArguments(1)
                        .and(ElementMatchers.takesArguments(String.class)
                                .or(ElementMatchers.takesArguments(injector.getIChatBaseComponentClass())
                                        .or(ElementMatchers.takesArguments(injector.getPacketLoginInEncryptionBeginClass())
                                        )
                                )
                        )
                ).intercept(
                        MethodDelegation.to(new HandleMethodInterceptor())
                )
                .make()
                .load(((MultiLoginBukkit) injector.getApi().getPlugin()).getMlPluginLoader()
                        .getPluginClassLoader().self(), ClassLoadingStrategy.Default.WRAPPER)
                .getLoaded();

        MethodHandles.Lookup lookup = MethodHandles.lookup();
        gameProfileGetter = lookup.unreflectGetter(ReflectUtil.handleAccessible(ReflectUtil.findNoStaticField(injector.getLoginListenerClass(), GameProfile.class)));
        Field field = proxyLoginListenerClass.getField(original_handler);
        multilogin_original_handlerFieldSetter = lookup.unreflectSetter(field);
        multilogin_original_handlerFieldGetter = lookup.unreflectGetter(field);
        chatComponentConstructor = lookup.unreflectConstructor(ReflectUtil.handleAccessible(injector.getChatComponentTextClass().getConstructor(String.class)));
    }

    public Object newFakeProxyLoginListener(Object source) throws Throwable {
        Constructor<?> constructor = proxyLoginListenerClass.getDeclaredConstructors()[0];
        Object[] parameters = new Object[constructor.getParameterCount()];
        for (int i = 0; i < constructor.getParameterTypes().length; i++) {
            parameters[i] = ReflectUtil.handleAccessible(ReflectUtil
                    .findNoStaticField(injector.getLoginListenerClass(), constructor.getParameterTypes()[i])).get(source);
        }
        Object o = constructor.newInstance(parameters);
        // 绑定 原来的 handler
        multilogin_original_handlerFieldSetter.invoke(o, source);
        return o;
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public class HandleMethodInterceptor {
        public Object intercept(
                @SuperCall Callable<Object> supercall,
                @This Object proxyObj,
                @Origin Method method,
                @AllArguments Object[] args) throws Throwable {
            return supercall.call();
        }
    }
}
