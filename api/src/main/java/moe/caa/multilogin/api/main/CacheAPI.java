package moe.caa.multilogin.api.main;

import moe.caa.multilogin.api.util.Pair;

import java.util.UUID;

/**
 * 简单缓存 API，所操作玩家需要在线。
 */
public interface CacheAPI {

    /**
     * 提交一个玩家退出事件
     *
     * @param inGameUUID 玩家的游戏内 UUID
     */
    void pushPlayerQuitGame(UUID inGameUUID, String username);

    /**
     * 提交一个玩家加入事件
     *
     * @param inGameUUID 玩家的游戏内 UUID
     */
    void pushPlayerJoinGame(UUID inGameUUID, String username);

    /**
     * 获得玩家的在线游戏档案
     *
     * @param inGameUUID 玩家的游戏内 UUID
     * @return 一个表示玩家在线 UUID 和验证它的 Yggdrasil ID 的复合类
     */
    Pair<UUID, Integer> getPlayerOnlineProfile(UUID inGameUUID);

    /**
     * 获得玩家游戏内 UUID
     *
     * @param onlineUUID  玩家的在线 UUID
     * @param yggdrasilId yggdrasil id
     * @return 玩家游戏内 UUID
     */
    UUID getInGameUUID(UUID onlineUUID, int yggdrasilId);
}
