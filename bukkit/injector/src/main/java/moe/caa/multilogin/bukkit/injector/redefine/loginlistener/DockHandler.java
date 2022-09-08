package moe.caa.multilogin.bukkit.injector.redefine.loginlistener;

import com.mojang.authlib.GameProfile;
import moe.caa.multilogin.bukkit.injector.BukkitInjector;

import java.net.SocketAddress;

public class DockHandler {
    public static Object[] handle(Object thisObj, Object[] args) throws Throwable {
        GameProfile profile = (GameProfile) BukkitInjector.getInjector().getLoginListener_gameProfileGetter().invoke(thisObj);
        // 修改方法参数
        if (args[0].getClass().equals(String.class) || BukkitInjector.getInjector().getIChatBaseComponentClass().isAssignableFrom(args[0].getClass())) {
            String s = BukkitInjector.getInjector().getLoginListenerData().getDisconnectMessage(Thread.currentThread());
            if (s != null) {
                if (args[0].getClass().equals(String.class)) {
                    return new Object[]{s};
                } else {
                    return new Object[]{BukkitInjector.getInjector().generateIChatBaseComponent(s)};
                }
            }
        }

        // 如果传入方法是 PacketLoginInEncryptionBegin ，先记录 SocketAddress
        if (BukkitInjector.getInjector().getPacketLoginInEncryptionBeginClass().isAssignableFrom(args[0].getClass())) {
            Object networkManager = BukkitInjector.getInjector().getLoginListener_networkManagerGetter().invoke(thisObj);
            SocketAddress address = (SocketAddress) BukkitInjector.getInjector().getLoginListener_socketAddressGetter().invoke(networkManager);
            BukkitInjector.getInjector().getLoginListenerData().setSocketAddress(profile, address);
        }
        return args;
    }
}
