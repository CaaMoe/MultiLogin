package moe.caa.multilogin.api.internal.plugin;

import org.jetbrains.annotations.ApiStatus;

import java.util.Set;
import java.util.UUID;

/**
 * 公共玩家管理器对象
 */
@ApiStatus.Internal
public interface IPlayerManager {

    /**
     * 以给定的名称返回在线的玩家
     *
     * @param name 给定的名称
     * @return 玩家执行者对象列表
     */
    Set<IPlayer> getPlayers(String name);

    /**
     * 以给定的唯一标识符返回在线的玩家
     *
     * @param uuid 给定的唯一标识符
     * @return 玩家执行者对象
     */
    IPlayer getPlayer(UUID uuid);

    /**
     * 获得当前所有在线的玩家列表
     *
     * @return 在线玩家列表
     */
    Set<IPlayer> getOnlinePlayers();

    /**
     * 踢出玩家，如果这名玩家存在的话
     *
     * @param name    玩家名称
     * @param message 踢出信息
     */
    default void kickPlayerIfOnline(String name, String message) {
        for (IPlayer player : getPlayers(name)) {
            player.kickPlayer(message);
        }
    }

    default void kickAll(String message) {
        for (IPlayer player : getOnlinePlayers()) {
            player.kickPlayer(message);
        }
    }

    /**
     * 踢出玩家，如果这名玩家存在的话
     *
     * @param uuid    玩家UUID
     * @param message 踢出信息
     */
    default void kickPlayerIfOnline(UUID uuid, String message) {
        IPlayer player = getPlayer(uuid);
        if (player != null) {
            player.kickPlayer(message);
        }
    }

    /**
     * 判断玩家是否在线
     *
     * @param redirectUuid 玩家的UUID
     * @return 玩家是否在线
     */
    default boolean hasOnline(UUID redirectUuid) {
        return getPlayer(redirectUuid) != null;
    }
}
