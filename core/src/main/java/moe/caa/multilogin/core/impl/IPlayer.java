package moe.caa.multilogin.core.impl;

import java.util.UUID;

public interface IPlayer extends ISender {
    /**
     * 以给定的理由踢出这名玩家
     *
     * @param message 给定的理由
     */
    void kickPlayer(String message);

    /**
     * 返回该名玩家的游戏内 UUID
     *
     * @return 玩家的游戏内 UUID
     */
    UUID getUniqueId();
}
