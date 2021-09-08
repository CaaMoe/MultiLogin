package moe.caa.multilogin.core.impl;

import java.util.Set;
import java.util.UUID;

/**
 * 代表一个玩家管理器对象
 */
public interface IPlayerManager {

    /**
     * 以给定的名称返回在线的玩家
     *
     * @param name 给定的名称
     * @return 玩家执行者对象列表
     */
    Set<ISender> getPlayer(String name);

    /**
     * 以给定的唯一标识符返回在线的玩家
     *
     * @param uuid 给定的唯一标识符
     * @return 玩家执行者对象
     */
    ISender getPlayer(UUID uuid);

    /**
     * 获得当前所有在线的玩家列表
     *
     * @return 在线玩家列表
     */
    Set<ISender> getOnlinePlayers();

    /**
     * 是否开启在线验证
     *
     * @return 在线验证模式
     */
    boolean isOnlineMode();

    /**
     * 判断是否开启原版的白名单系统
     */
    boolean isWhitelist();
}
