package moe.caa.multilogin.bukkit.injector;

import lombok.AllArgsConstructor;
import lombok.Getter;
import moe.caa.multilogin.api.logger.LoggerProvider;
import moe.caa.multilogin.bukkit.injector.proxy.PacketLoginInEncryptionBeginInvocationHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LoginListenerSynchronizer {
    @Getter
    private static final LoginListenerSynchronizer instance = new LoginListenerSynchronizer();
    private final Map<Object, OriginEntry> proxyLoginListenerMap = new ConcurrentHashMap<>();

    public void putEntry(Object proxyObj, Object origin) {
        proxyLoginListenerMap.put(proxyObj, new OriginEntry(origin, System.currentTimeMillis() + 1000 * 10));
    }

    protected void init() {
        BukkitInjector.getApi().getPlugin().getRunServer().getScheduler().runTaskAsyncTimer(this::sync, 0, 500);
    }

    private void sync() {
        long currentTimeMillis = System.currentTimeMillis();
        proxyLoginListenerMap.entrySet().removeIf(e -> {
            try {
                PacketLoginInEncryptionBeginInvocationHandler.apply(BukkitInjector.getLoginListenerClass(), e.getKey(), e.getValue().origin);
            } catch (IllegalAccessException ex) {
                LoggerProvider.getLogger().error("Proxy object data cannot be synchronized.", ex);
            }
            return e.getValue().invalidTimeMillis < currentTimeMillis;
        });
    }

    @AllArgsConstructor
    private static class OriginEntry {
        private final Object origin;
        private final long invalidTimeMillis;
    }
}
