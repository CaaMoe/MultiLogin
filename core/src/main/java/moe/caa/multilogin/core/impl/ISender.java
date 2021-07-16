package moe.caa.multilogin.core.impl;

import java.util.UUID;

/**
 * 代表一个命令执行者对象
 */
public interface ISender {

    /**
     * 获得执行者名称
     *
     * @return 执行者名称
     */
    String getName();

    /**
     * 获得执行者有没有某权限
     *
     * @param permission 权限
     * @return 执行者是否有某权限
     */
    boolean hasPermission(String permission);

    /**
     * 给执行者发送特定的字符串消息
     *
     * @param message 发送的字符串消息
     */
    void sendMessage(String message);

    /**
     * 判断执行者是否是操作员
     *
     * @return 执行者是否是操作员
     */
    boolean isOp();

    /**
     * 判断执行者是否是一名实体玩家
     *
     * @return 执行者是否是一名实体玩家
     */
    boolean isPlayer();

    /**
     * 获得执行者对应的玩家的UniqueIdentifier
     *
     * @return 对应的玩家的UniqueIdentifier
     */
    UUID getPlayerUniqueIdentifier();

    /**
     * 以给定的字符串消息踢出该名执行者对象
     *
     * @param message 字符串消息
     * @return 踢出是否成功
     */
    boolean kickPlayer(String message);
}
