package moe.caa.multilogin.bukkit.injector.subclasshandler;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import moe.caa.multilogin.bukkit.injector.BukkitInjector;
import moe.caa.multilogin.bukkit.main.MultiLoginBukkit;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * 代理 PacketLoginInEncryptionBegin 这个数据包
 */
public class PacketLoginInEncryptionBeginSubclassHandler {
    private final BukkitInjector injector;
    @Getter
    private Class<?> proxyLoginInEncryptionBeginClass;
    private MethodHandle multilogin_original_handlerFieldSetter;
    private MethodHandle multilogin_original_handlerFieldGetter;

    public PacketLoginInEncryptionBeginSubclassHandler(BukkitInjector injector) {
        this.injector = injector;
    }

    public void init() throws NoSuchFieldException, IllegalAccessException {
        String name = "moe.caa.multilogin.bukkit.injector.PacketLoginInEncryptionBegin$" + UUID.randomUUID().toString().substring(0, 6).toUpperCase(Locale.ROOT);
        String original_handler = "multilgoin_original_handler";
        proxyLoginInEncryptionBeginClass = new ByteBuddy()
                .subclass(injector.getPacketLoginInEncryptionBeginClass())
                .name(name)
                .defineField(original_handler, injector.getPacketLoginInEncryptionBeginClass(), Visibility.PUBLIC)
                .method(ElementMatchers.takesArguments(1)
                        .and(ElementMatchers.takesArguments(injector.getPacketLoginInListenerClass())))
                .intercept(MethodDelegation.to(new HandleMethodInterceptor()))
                .make()
                .load(((MultiLoginBukkit) injector.getApi().getPlugin()).getMlPluginLoader()
                        .getPluginClassLoader().self(), ClassLoadingStrategy.Default.WRAPPER)
                .getLoaded();

        MethodHandles.Lookup lookup = MethodHandles.lookup();
        Field field = proxyLoginInEncryptionBeginClass.getField(original_handler);
        multilogin_original_handlerFieldSetter = lookup.unreflectSetter(field);
        multilogin_original_handlerFieldGetter = lookup.unreflectGetter(field);
    }

    @SneakyThrows
    public Object newProxyLoginInEncryptionBegin(Object... args) {
        return proxyLoginInEncryptionBeginClass.getDeclaredConstructors()[0].newInstance(args);
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public class HandleMethodInterceptor {

        public Object intercept(
                @SuperCall Callable<Object> supercall,
                @AllArguments Object[] args) throws Throwable {
            @Advice.AllArguments
                    args[0] = null;
            return supercall.call();
        }
    }
}
