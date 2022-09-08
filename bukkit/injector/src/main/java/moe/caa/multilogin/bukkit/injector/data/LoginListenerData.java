package moe.caa.multilogin.bukkit.injector.data;

import com.mojang.authlib.GameProfile;
import lombok.AllArgsConstructor;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LoginListenerData {
    private final Map<Thread, Entry<String>> disconnectMessageMap = new ConcurrentHashMap<>();
    private final Map<GameProfile, Entry<SocketAddress>> socketAddressMap = new ConcurrentHashMap<>();

    public void setDisconnectMessage(Thread authThread, String disconnectMessage) {
        disconnectMessageMap.put(authThread, new Entry<>(disconnectMessage, System.currentTimeMillis() + 1000 * 10));
    }

    public String getDisconnectMessage(Thread authThread) {
        long currentTimeMillis = System.currentTimeMillis();
        disconnectMessageMap.entrySet().removeIf(e -> e.getValue().invalidTimeMillis < currentTimeMillis);
        Entry<String> entry = disconnectMessageMap.remove(authThread);
        return entry == null ? null : entry.value;
    }

    public void setSocketAddress(GameProfile profile, SocketAddress address) {
        socketAddressMap.put(profile, new Entry<>(address, System.currentTimeMillis() + 1000 * 10));
    }

    public SocketAddress getSocketAddress(GameProfile profile) {
        long currentTimeMillis = System.currentTimeMillis();
        socketAddressMap.entrySet().removeIf(e -> e.getValue().invalidTimeMillis < currentTimeMillis);
        Entry<SocketAddress> entry = socketAddressMap.remove(profile);
        return entry == null ? null : entry.value;
    }


    @AllArgsConstructor
    private static class Entry<V> {
        private V value;
        private long invalidTimeMillis;
    }
}
