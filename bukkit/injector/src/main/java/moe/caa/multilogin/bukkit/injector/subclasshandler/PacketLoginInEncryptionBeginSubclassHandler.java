package moe.caa.multilogin.bukkit.injector.subclasshandler;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import moe.caa.multilogin.bukkit.injector.BukkitInjector;
import moe.caa.multilogin.bukkit.main.MultiLoginBukkit;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Morph;
import net.bytebuddy.matcher.ElementMatchers;

import java.util.Locale;
import java.util.UUID;

/**
 * 代理 PacketLoginInEncryptionBegin 这个数据包
 */
public class PacketLoginInEncryptionBeginSubclassHandler {
    private final BukkitInjector injector;
    @Getter
    private Class<?> proxyLoginInEncryptionBeginClass;

    public PacketLoginInEncryptionBeginSubclassHandler(BukkitInjector injector) {
        this.injector = injector;
    }

    public void init() {
        String name = "moe.caa.multilogin.bukkit.injector.PacketLoginInEncryptionBegin$" + UUID.randomUUID().toString().substring(0, 6).toUpperCase(Locale.ROOT);
        proxyLoginInEncryptionBeginClass = new ByteBuddy()
                .subclass(injector.getPacketLoginInEncryptionBeginClass())
                .name(name)
                .method(ElementMatchers.takesArguments(1)
                        .and(ElementMatchers.takesArguments(injector.getPacketLoginInListenerClass())))
                .intercept(MethodDelegation.withDefaultConfiguration()
                        .withBinders(Morph.Binder.install(Morpher.class))
                        .to(new ModifyLoginListenerArg()))
                .make()
                .load(((MultiLoginBukkit) injector.getApi().getPlugin()).getMlPluginLoader()
                        .getPluginClassLoader().self(), ClassLoadingStrategy.Default.WRAPPER)
                .getLoaded();
    }

    @SneakyThrows
    public Object newProxyLoginInEncryptionBegin(Object... args) {
        return proxyLoginInEncryptionBeginClass.getDeclaredConstructors()[0].newInstance(args);
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
