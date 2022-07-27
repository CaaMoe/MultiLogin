package moe.caa.multilogin.core.main;

import lombok.AllArgsConstructor;
import lombok.Getter;
import moe.caa.multilogin.api.logger.LoggerProvider;
import moe.caa.multilogin.api.main.CacheAPI;
import moe.caa.multilogin.api.util.Pair;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerCache implements CacheAPI {

    // inGameUUID \ Entry
    private final Map<UUID, Entry> cache;

    // inGameUUID \ Entry
    // 表示登录缓存
    @Getter
    private final Map<UUID, Entry> loginCache;

    public PlayerCache() {
        cache = new ConcurrentHashMap<>();
        loginCache = new ConcurrentHashMap<>();
    }

    @Override
    public void pushPlayerQuitGame(UUID inGameUUID, String username) {
        cache.remove(inGameUUID);
    }

    @Override
    public void pushPlayerJoinGame(UUID inGameUUID, String username) {
        Entry remove = loginCache.remove(inGameUUID);
        if (remove == null) {
            LoggerProvider.getLogger().warn(String.format(
                    "The player with in game UUID %s and name %s is not logged into the server by MultiLogin, some features will be disabled for him.",
                    inGameUUID.toString(), username
            ));
            return;
        }
        long l = System.currentTimeMillis() - remove.signTimeMillis;
        if (l > 5 * 1000) {
            LoggerProvider.getLogger().warn(String.format(
                    "Players with in game UUID %s and name %s are taking too long to log in after verification, reached %d milliseconds. Is it the same person?",
                    inGameUUID.toString(), username, l
            ));
        }

        cache.put(inGameUUID, remove);
    }

    @Override
    public Pair<UUID, Integer> getPlayerOnlineProfile(UUID inGameUUID) {
        Entry entry = cache.get(inGameUUID);
        if (entry == null) return null;
        return new Pair<>(entry.onlineUUID, entry.yggdrasilID);
    }

    @AllArgsConstructor
    public static class Entry {
        private final UUID onlineUUID;
        private final int yggdrasilID;
        private final long signTimeMillis;
    }
}
