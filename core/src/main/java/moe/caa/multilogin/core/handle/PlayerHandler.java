package moe.caa.multilogin.core.handle;

import lombok.AllArgsConstructor;
import lombok.Getter;
import moe.caa.multilogin.api.handle.HandleResult;
import moe.caa.multilogin.api.handle.HandlerAPI;
import moe.caa.multilogin.api.logger.LoggerProvider;
import moe.caa.multilogin.api.plugin.IPlayer;
import moe.caa.multilogin.api.util.Pair;
import moe.caa.multilogin.core.configuration.yggdrasil.YggdrasilServiceConfig;
import moe.caa.multilogin.core.main.MultiCore;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 数据缓存中心
 */
public class PlayerHandler implements HandlerAPI {

    private final MultiCore core;

    // inGameUUID \ Entry
    private final Map<UUID, Entry> cache;

    // inGameUUID \ Entry
    // 表示登录缓存
    @Getter
    private final Map<UUID, Entry> loginCache;

    public PlayerHandler(MultiCore core) {
        this.core = core;
        this.cache = new ConcurrentHashMap<>();
        this.loginCache = new ConcurrentHashMap<>();
    }

    @Override
    public HandleResult pushPlayerQuitGame(UUID inGameUUID, String username) {
        return new HandleResult(HandleResult.Type.NONE, null);
    }

    @Override
    public HandleResult pushPlayerJoinGame(UUID inGameUUID, String username) {
        Entry remove = loginCache.remove(inGameUUID);
        if (remove == null) {
            if (core.getPluginConfig().isForceUseLogin()) {
                return new HandleResult(HandleResult.Type.KICK, core.getLanguageHandler().getMessage("auth_handler_need_use_login"));
            }
            LoggerProvider.getLogger().warn(String.format(
                    "The player with in game UUID %s and name %s is not logged into the server by MultiLogin, some features will be disabled for him.",
                    inGameUUID.toString(), username
            ));
        } else {
            long l = System.currentTimeMillis() - remove.signTimeMillis;
            if (l > 5 * 1000) {
                LoggerProvider.getLogger().warn(String.format(
                        "Players with in game UUID %s and name %s are taking too long to log in after verification, reached %d milliseconds. Is it the same person?",
                        inGameUUID.toString(), username, l
                ));
            }
            cache.put(inGameUUID, remove);
        }

        return new HandleResult(HandleResult.Type.NONE, null);
    }

    @Override
    public Pair<UUID, Integer> getPlayerOnlineProfile(UUID inGameUUID) {
        Entry entry = cache.get(inGameUUID);
        if (entry == null) return null;
        return new Pair<>(entry.onlineUUID, entry.yggdrasilID);
    }

    @Override
    public UUID getInGameUUID(UUID onlineUUID, int yggdrasilId) {
        for (Map.Entry<UUID, Entry> entry : cache.entrySet()) {
            if (entry.getValue().onlineUUID.equals(onlineUUID) && entry.getValue().yggdrasilID == yggdrasilId)
                return entry.getKey();
        }
        return null;
    }

    @Override
    public String getYggdrasilName(int yggdrasilId) {
        YggdrasilServiceConfig config = core.getPluginConfig().getIdMap().get(yggdrasilId);
        if (config == null) return null;
        return config.getName();
    }

    public void register() {
        core.getPlugin().getRunServer().getScheduler().runTaskAsyncTimer(() -> {
            // 存放在线的所有玩家
            Set<UUID> onlinePlayerUUIDs = core.getPlugin().getRunServer().getPlayerManager().getOnlinePlayers().stream()
                    .map(IPlayer::getUniqueId).collect(Collectors.toSet());

            // 遍历当前缓存，获取失效的数据列表
            Set<Map.Entry<UUID, Entry>> noExists = cache.entrySet().stream().filter(e -> !onlinePlayerUUIDs.contains(e.getKey())).collect(Collectors.toSet());

            try {
                Thread.sleep(1000 * 10);
            } catch (InterruptedException e) {
                LoggerProvider.getLogger().error("An exception occurred on the delayed cache clearing.", e);
            }

            // 移除失效的数据
            for (Map.Entry<UUID, Entry> e : noExists) {
                Entry entry = cache.get(e.getKey());

                // 数据已被移除
                if (entry == null) continue;

                // 在移除前数据被更改
                if (!e.getValue().equals(entry)) continue;

                // 进行移除
                cache.remove(e.getKey());
            }

        }, 0, 1000 * 60);
    }

    @AllArgsConstructor
    public static class Entry {
        private final UUID onlineUUID;
        private final int yggdrasilID;
        private final long signTimeMillis;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Entry entry = (Entry) o;
            return yggdrasilID == entry.yggdrasilID && signTimeMillis == entry.signTimeMillis && Objects.equals(onlineUUID, entry.onlineUUID);
        }

        @Override
        public int hashCode() {
            return Objects.hash(onlineUUID, yggdrasilID, signTimeMillis);
        }
    }
}
