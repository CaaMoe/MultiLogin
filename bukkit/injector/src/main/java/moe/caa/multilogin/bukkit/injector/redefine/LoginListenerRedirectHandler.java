package moe.caa.multilogin.bukkit.injector.redefine;

import com.mojang.authlib.GameProfile;
import moe.caa.multilogin.bukkit.injector.BukkitInjector;
import moe.caa.multilogin.bukkit.injector.Contents;
import moe.caa.multilogin.bukkit.main.MultiLoginBukkit;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.implementation.bind.annotation.This;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatchers;

import java.net.SocketAddress;

/**
 * 子类重写、代理 LoginListener 这个类
 */
public class LoginListenerRedirectHandler {
    public void init() {
        new ByteBuddy()
                .redefine(BukkitInjector.getInjector().getLoginListenerClass())
                .visit(Advice.to(HandleMethodInterceptor.class).on(ElementMatchers.takesArguments(1)
                        .and(ElementMatchers.takesArguments(String.class)
                                .or(ElementMatchers.takesArguments(BukkitInjector.getInjector().getIChatBaseComponentClass())
                                        .or(ElementMatchers.takesArguments(BukkitInjector.getInjector().getPacketLoginInEncryptionBeginClass()))
                                )
                        )))
                .make()
                .load(((MultiLoginBukkit) BukkitInjector.getInjector().getApi().getPlugin()).getMlPluginLoader().getPluginClassLoader().self(), ClassReloadingStrategy.of(ByteBuddyAgent.install()))
                .getLoaded();
    }

    public static class HandleMethodInterceptor {

        @Advice.OnMethodEnter
        public static void intercept(
                @This Object proxyObj,
                @Advice.AllArguments(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object[] args
        ) throws Throwable {
            GameProfile profile = (GameProfile) BukkitInjector.getInjector().getLoginListener_gameProfileGetter().invoke(proxyObj);
            // 修改方法参数
            if (args[0].getClass().equals(String.class) || BukkitInjector.getInjector().getIChatBaseComponentClass().isAssignableFrom(args[0].getClass())) {
                Contents.KickMessageEntry remove = Contents.getKickMessageEntryMap().remove(profile.getName());
                if (remove != null) {
                    if (args[0].getClass().equals(String.class)) {
                        args = new Object[]{remove.getKickMessage()};
                    } else {
                        args = new Object[]{BukkitInjector.getInjector().generateIChatBaseComponent(remove.getKickMessage())};
                    }
                }
            }

            // 如果传入方法是 PacketLoginInEncryptionBegin ，先记录 SocketAddress
            if (BukkitInjector.getInjector().getPacketLoginInEncryptionBeginClass().isAssignableFrom(args[0].getClass())) {
                Object networkManager = BukkitInjector.getInjector().getLoginListener_networkManagerGetter().invoke(proxyObj);
                SocketAddress address = (SocketAddress) BukkitInjector.getInjector().getLoginListener_socketAddressGetter().invoke(networkManager);
                BukkitInjector.getInjector().getLoginStateSocketAddressGetter().put(profile.getName(), address);
            }
        }
    }
}
