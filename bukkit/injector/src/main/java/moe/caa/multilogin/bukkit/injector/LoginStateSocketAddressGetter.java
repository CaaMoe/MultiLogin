package moe.caa.multilogin.bukkit.injector;

import lombok.AllArgsConstructor;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 登录验证时记录 IP 地址的操作类
 */
public class LoginStateSocketAddressGetter {
    private final Map<Object, Entry> addressGetterMap = new ConcurrentHashMap<>();

    public void put(Object obj, SocketAddress address) {
        addressGetterMap.put(obj, new Entry(address, System.currentTimeMillis() + 1000 * 10));
    }

    public SocketAddress get(Object obj) {
        long currentTimeMillis = System.currentTimeMillis();
        Entry entry = addressGetterMap.get(obj);
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
