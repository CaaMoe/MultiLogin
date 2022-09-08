package moe.caa.multilogin.bukkit.injector.redefine.loginlistener;

import moe.caa.multilogin.bukkit.injector.BukkitInjector;
import moe.caa.multilogin.bukkit.injector.Contents;

import java.net.SocketAddress;

public class DockHandler {
    public static Object[] handle(Object thisObj, Object[] args) throws Throwable {
        // 修改方法参数
        if (args[0].getClass().equals(String.class) || BukkitInjector.getInjector().getIChatBaseComponentClass().isAssignableFrom(args[0].getClass())) {
            Contents.KickMessageEntry remove = Contents.getKickMessageEntryMap().remove(thisObj);
            if (remove != null) {
                if (args[0].getClass().equals(String.class)) {
                    return new Object[]{remove.getKickMessage()};
                } else {
                    return new Object[]{BukkitInjector.getInjector().generateIChatBaseComponent(remove.getKickMessage())};
                }
            }
        }

        // 如果传入方法是 PacketLoginInEncryptionBegin ，先记录 SocketAddress
        if (BukkitInjector.getInjector().getPacketLoginInEncryptionBeginClass().isAssignableFrom(args[0].getClass())) {
            Object networkManager = BukkitInjector.getInjector().getLoginListener_networkManagerGetter().invoke(thisObj);
            SocketAddress address = (SocketAddress) BukkitInjector.getInjector().getLoginListener_socketAddressGetter().invoke(networkManager);
            BukkitInjector.getInjector().getLoginStateSocketAddressGetter().put(thisObj, address);
        }
        return args;
    }
}
