package moe.caa.multilogin.api.plugin;

import moe.caa.multilogin.api.auth.GameProfile;
import moe.caa.multilogin.api.util.Pair;

import java.net.SocketAddress;
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

    /**
     * 返回该名玩家的 IP 地址
     *
     * @return 玩家的 IP 地址
     */
    SocketAddress getAddress();

    /**
     *  重设该玩家的档案信息
     * @throws Throwable 反射调用过程中可能会出现异常
     */
    void resetGameProfile(GameProfile profile) throws Throwable;

    /**
     * 让玩家重连
     * @throws Throwable 反射调用过程中可能会出现异常
     */
    void reconnect() throws Throwable;


}
