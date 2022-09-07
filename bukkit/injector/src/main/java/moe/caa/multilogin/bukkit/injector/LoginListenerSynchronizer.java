package moe.caa.multilogin.bukkit.injector;

import lombok.AllArgsConstructor;
import moe.caa.multilogin.api.logger.LoggerProvider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * LoginListener 及其他的代理的字段同步程序
 */
public class LoginListenerSynchronizer {
    private final BukkitInjector injector;
    private final Map<Object, OriginEntry> proxyLoginListenerMap = new ConcurrentHashMap<>();

    public LoginListenerSynchronizer(BukkitInjector injector) {
        this.injector = injector;
    }

    /**
     * 注册一对，同步 10 秒
     */
    public void putEntry(Object proxyObj, Object origin) {
        proxyLoginListenerMap.put(proxyObj, new OriginEntry(origin, System.currentTimeMillis() + 1000 * 10));
    }

    protected void init() {
        injector.getApi().getPlugin().getRunServer().getScheduler().runTaskAsyncTimer(this::sync, 0, 500);
    }

    private void sync() {
        long currentTimeMillis = System.currentTimeMillis();
        proxyLoginListenerMap.entrySet().removeIf(e -> {
            try {
                InjectUtil.apply(injector.getLoginListenerClass(), e.getKey(), e.getValue().origin);
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
