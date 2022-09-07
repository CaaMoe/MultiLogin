package moe.caa.multilogin.bukkit.injector.subclasshandler;

import com.mojang.authlib.GameProfile;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import moe.caa.multilogin.api.util.reflect.ReflectUtil;
import moe.caa.multilogin.bukkit.injector.BukkitInjector;
import moe.caa.multilogin.bukkit.injector.Contents;
import moe.caa.multilogin.bukkit.injector.InjectUtil;
import moe.caa.multilogin.bukkit.main.MultiLoginBukkit;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Morph;
import net.bytebuddy.implementation.bind.annotation.This;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Locale;
import java.util.UUID;

public class LoginListenerSubclassHandler {
    private final BukkitInjector injector;
    @Getter
    private Class<?> proxyLoginListenerClass;
    private MethodHandle multilogin_original_handlerFieldSetter;
    private MethodHandle multilogin_original_handlerFieldGetter;
    private MethodHandle gameProfileGetter;

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
                // 代理方法
                .method(ElementMatchers.takesArguments(1)
                        .and(ElementMatchers.takesArguments(String.class)
                                .or(ElementMatchers.takesArguments(injector.getIChatBaseComponentClass())
                                        .or(ElementMatchers.takesArguments(injector.getPacketLoginInEncryptionBeginClass()))
                                )
                        )).intercept(
                        MethodDelegation.withDefaultConfiguration()
                                .withBinders(Morph.Binder.install(Morpher.class))
                                .to(new HandleMethodInterceptor()))
                .make()
                .load(((MultiLoginBukkit) injector.getApi().getPlugin()).getMlPluginLoader()
                        .getPluginClassLoader().self(), ClassLoadingStrategy.Default.WRAPPER)
                .getLoaded();

        MethodHandles.Lookup lookup = MethodHandles.lookup();
        gameProfileGetter = lookup.unreflectGetter(ReflectUtil.handleAccessible(ReflectUtil.findNoStaticField(injector.getLoginListenerClass(), GameProfile.class)));
        Field field = proxyLoginListenerClass.getField(original_handler);
        multilogin_original_handlerFieldSetter = lookup.unreflectSetter(field);
        multilogin_original_handlerFieldGetter = lookup.unreflectGetter(field);
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

    public interface Morpher {
        Object invoke(Object[] args);
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public class HandleMethodInterceptor {
        public Object intercept(
                @Morph Morpher morpher,
                @This Object proxyObj,
                @AllArguments Object[] args) throws Throwable {


            Object origin = multilogin_original_handlerFieldGetter.invoke(proxyObj);
            // 调用前，同步原对象的数据
            InjectUtil.apply(injector.getLoginListenerClass(), origin, proxyObj);

            // 修改方法参数
            if (args[0].getClass().equals(String.class) || injector.getIChatBaseComponentClass().isAssignableFrom(args[0].getClass())) {
                GameProfile profile = (GameProfile) gameProfileGetter.invoke(proxyObj);
                Contents.KickMessageEntry remove = Contents.getKickMessageEntryMap().remove(profile.getName());
                if (remove != null) {
                    if (args[0].getClass().equals(String.class)) {
                        args[0] = remove.getKickMessage();
                    } else {
                        args[0] = injector.generateIChatBaseComponent(remove.getKickMessage());
                    }
                }
            }

            // 开始调用
            Object invoke = morpher.invoke(args);

            // 调用后，同步原对象的数据
            InjectUtil.apply(injector.getLoginListenerClass(), proxyObj, origin);

            // 如果传入方法是 PacketLoginInEncryptionBegin ，提交到同步器
            if (injector.getPacketLoginInEncryptionBeginClass().isAssignableFrom(args[0].getClass())) {
                injector.getLoginListenerSynchronizer().putEntry(proxyObj, origin);
            }
            return invoke;
        }
    }
}
