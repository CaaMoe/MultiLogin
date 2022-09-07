package moe.caa.multilogin.bukkit.injector;

import lombok.AllArgsConstructor;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 登录验证时记录 IP 地址的操作类
 */
public class LoginStateSocketAddressGetter {
    private final BukkitInjector injector;
    private final Map<String, Entry> addressGetterMap = new ConcurrentHashMap<>();

    public LoginStateSocketAddressGetter(BukkitInjector injector) {
        this.injector = injector;
    }

    public void put(String name, SocketAddress address) {
        addressGetterMap.put(name, new Entry(address, System.currentTimeMillis() + 1000 * 10));
    }

    public SocketAddress get(String name) {
        long currentTimeMillis = System.currentTimeMillis();
        Entry entry = addressGetterMap.get(name);
        addressGetterMap.entrySet().removeIf(e -> e.getValue().invalidTimeMillis < currentTimeMillis);
        if (entry != null) return entry.socketAddress;
        return null;
    }

    @AllArgsConstructor
    private static class Entry {
        private final SocketAddress socketAddress;
        private final long invalidTimeMillis;
    }
}
