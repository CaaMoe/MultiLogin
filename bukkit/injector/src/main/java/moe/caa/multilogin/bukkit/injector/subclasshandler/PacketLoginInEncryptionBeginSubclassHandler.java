package moe.caa.multilogin.bukkit.injector.subclasshandler;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import moe.caa.multilogin.api.logger.LoggerProvider;
import moe.caa.multilogin.bukkit.injector.BukkitInjector;
import moe.caa.multilogin.bukkit.main.MultiLoginBukkit;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Morph;
import net.bytebuddy.matcher.ElementMatchers;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;
import java.util.UUID;

/**
 * 子类重写、代理 PacketLoginInEncryptionBegin 这个数据包
 */
public class PacketLoginInEncryptionBeginSubclassHandler {
    private final BukkitInjector injector;
    @Getter
    private Class<?> proxyLoginInEncryptionBeginClass;

    public PacketLoginInEncryptionBeginSubclassHandler(BukkitInjector injector) {
        this.injector = injector;
    }

    public void init() throws IOException {
        String name = "moe.caa.multilogin.bukkit.injector.PacketLoginInEncryptionBegin$" + UUID.randomUUID().toString().substring(0, 6).toUpperCase(Locale.ROOT);
        DynamicType.Loaded<?> loaded = new ByteBuddy()
                .subclass(injector.getPacketLoginInEncryptionBeginClass())
                .name(name)
                .method(ElementMatchers.takesArguments(1)
                        .and(ElementMatchers.takesArguments(injector.getPacketLoginInListenerClass())))
                .intercept(MethodDelegation.withDefaultConfiguration()
                        .withBinders(Morph.Binder.install(Morpher.class))
                        .to(new ModifyLoginListenerArg()))
                .make()
                .load(((MultiLoginBukkit) injector.getApi().getPlugin()).getMlPluginLoader()
                        .getPluginClassLoader().self(), ClassLoadingStrategy.Default.WRAPPER);
        proxyLoginInEncryptionBeginClass = loaded
                .getLoaded();
        loaded.saveIn(new File("output"));
    }

    @SneakyThrows
    public Object newProxyLoginInEncryptionBegin(Object... args) {
        try {
            Iterator<Constructor<?>> iterator = Arrays.stream(proxyLoginInEncryptionBeginClass.getDeclaredConstructors()).iterator();
            lab:
            while (iterator.hasNext()) {
                Constructor<?> next = iterator.next();
                if (next.getParameterTypes().length != args.length) {
                    continue;
                }

                for (int i = 0; i < next.getParameterTypes().length; i++) {
                    if (!next.getParameterTypes()[i].isAssignableFrom(args[i].getClass())) {
                        break lab;
                    }
                }
                return next.newInstance(args);
            }
            Constructor<?> constructor = proxyLoginInEncryptionBeginClass.getDeclaredConstructors()[0];
            return constructor.newInstance(args);
        } catch (Throwable throwable) {
            LoggerProvider.getLogger().error(throwable);
            throw throwable;
        }
    }

    public interface Morpher {
        Object invoke(Object[] args);
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public class ModifyLoginListenerArg {

        public Object intercept(
                @Morph Morpher morpher, @AllArguments Object[] args) throws Throwable {
            return morpher.invoke(new Object[]{injector.getLoginListenerSubclassHandler().newFakeProxyLoginListener(args[0])});
        }
    }
}
