package moe.caa.multilogin.core.impl;

import java.util.UUID;

/**
 * 代表一个命令执行者对象
 */
public interface ISender {

    /**
     * 这个命令执行者是不是一位玩家
     * @return 是不是玩家
     */
    boolean isPlayer();

    /**
     * 这个命令执行者是否具有某权限
     * @return 是否具有某权限
     */
    boolean hasPermission(String permission);

    /**
     * 给执行者发送特定的字符串消息
     *
     * @param message 发送的字符串消息
     */
    void sendMessage(String message);

    /**
     * 如果这个命令执行者是一名玩家，则以给定的理由踢出这名玩家
     * @param message 给定的理由
     */
    void kickPlayer(String message);

    /**
     * 获得这个命令执行者的名称
     * @return 命令执行者的名称
     */
    String getName();

    /**
     * 如果这个命令执行者是一名玩家，则返回改名玩家的游戏内 UUID
     * @return 玩家的游戏内 UUID，否则为 NULL
     */
    UUID getPlayerUid();
}
