package moe.caa.multilogin.core.handle;

import lombok.AllArgsConstructor;
import lombok.Getter;
import moe.caa.multilogin.api.data.MultiLoginPlayerData;
import moe.caa.multilogin.api.profile.GameProfile;
import moe.caa.multilogin.api.internal.handle.HandleResult;
import moe.caa.multilogin.api.internal.handle.HandlerAPI;
import moe.caa.multilogin.api.internal.logger.LoggerProvider;
import moe.caa.multilogin.api.internal.plugin.IPlayer;
import moe.caa.multilogin.api.internal.util.Pair;
import moe.caa.multilogin.api.service.IService;
import moe.caa.multilogin.core.configuration.service.BaseServiceConfig;
import moe.caa.multilogin.core.main.MultiCore;
import org.jetbrains.annotations.NotNull;

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
    public void callPlayerJoinGame(IPlayer player) {
        if (!core.getPluginConfig().isWelcomeMsg()) {
            return;
        }

        core.getPlugin().getRunServer().getScheduler().runTaskAsync(() -> {
            Pair<GameProfile, BaseServiceConfig> pair = getPlayerOnlineProfile0(player.getUniqueId());
            String msg;
            if (pair == null) {
                msg = core.getLanguageHandler().getMessage("welcome_msg_to_unknown",
                        new Pair<>("profile_name", player.getName()),
                        new Pair<>("profile_uuid", player.getName()));
            } else {
                msg = core.getLanguageHandler().getMessage("welcome_msg",
                        new Pair<>("online_name", pair.getValue1().getName()),
                        new Pair<>("online_uuid", pair.getValue1().getId()),
                        new Pair<>("service_name", pair.getValue2().getName()),
                        new Pair<>("service_id", pair.getValue2().getId()),
                        new Pair<>("profile_name", player.getName()),
                        new Pair<>("profile_uuid", player.getUniqueId())
                );
            }
            player.sendMessagePL(msg);
        }, 3000);
    }

    public MultiLoginPlayerData getPlayerData(UUID inGameUUID){
        return cache.get(inGameUUID);
    }

    @Override
    public Pair<GameProfile, Integer> getPlayerOnlineProfile(UUID inGameUUID) {
        Entry entry = cache.get(inGameUUID);
        if (entry == null) return null;
        return new Pair<>(entry.onlineProfile, entry.serviceConfig.getId());
    }

    public Pair<GameProfile, BaseServiceConfig> getPlayerOnlineProfile0(UUID inGameUUID) {
        Entry entry = cache.get(inGameUUID);
        if (entry == null) return null;
        return new Pair<>(entry.onlineProfile, entry.serviceConfig);
    }

    @Override
    public UUID getInGameUUID(UUID onlineUUID, int serviceId) {
        for (Map.Entry<UUID, Entry> entry : cache.entrySet()) {
            if (entry.getValue().onlineProfile.getId().equals(onlineUUID)
                    && entry.getValue().serviceConfig.getId() == serviceId)
                return entry.getKey();
        }
        return null;
    }

    @Override
    public String getServiceName(int serviceId) {
        BaseServiceConfig config = core.getPluginConfig().getServiceIdMap().get(serviceId);
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
    public static class Entry implements MultiLoginPlayerData {
        private final GameProfile onlineProfile;
        private final BaseServiceConfig serviceConfig;
        private final long signTimeMillis;

        @NotNull
        @Override
        public GameProfile getOnlineProfile() {
            return onlineProfile;
        }

        @NotNull
        @Override
        public IService getLoginService() {
            return serviceConfig;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Entry entry = (Entry) o;
            return Objects.equals(serviceConfig, entry.serviceConfig) && signTimeMillis == entry.signTimeMillis && Objects.equals(onlineProfile, entry.onlineProfile);
        }

        @Override
        public int hashCode() {
            return Objects.hash(onlineProfile, serviceConfig, signTimeMillis);
        }
    }
}
