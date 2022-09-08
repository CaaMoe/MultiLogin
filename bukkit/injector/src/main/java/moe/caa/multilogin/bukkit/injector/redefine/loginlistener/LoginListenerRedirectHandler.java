package moe.caa.multilogin.bukkit.injector.redefine.loginlistener;

import moe.caa.multilogin.bukkit.injector.BukkitInjector;
import moe.caa.multilogin.bukkit.main.MultiLoginBukkit;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * 子类重写、代理 LoginListener 这个类
 */
public class LoginListenerRedirectHandler {
    public void init() {
        new ByteBuddy()
                .redefine(BukkitInjector.getInjector().getLoginListenerClass())
                .visit(Advice.to(HandleMethodInterceptor.class).on(ElementMatchers.takesArguments(1)
                        .and(ElementMatchers.takesArguments(String.class)
                                .and(ElementMatchers.not(ElementMatchers.isStatic()))
                                .or(ElementMatchers.takesArguments(BukkitInjector.getInjector().getIChatBaseComponentClass())
                                        .or(ElementMatchers.takesArguments(BukkitInjector.getInjector().getPacketLoginInEncryptionBeginClass()))
                                )
                        )))
                .make()
                .load(((MultiLoginBukkit) BukkitInjector.getInjector().getApi().getPlugin()).getMlPluginLoader().getPluginClassLoader().self(), ClassReloadingStrategy.of(ByteBuddyAgent.install()))
                .getLoaded();
    }
}
