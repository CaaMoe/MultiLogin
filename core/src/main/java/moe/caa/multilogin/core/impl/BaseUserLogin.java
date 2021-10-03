package moe.caa.multilogin.core.impl;

import moe.caa.multilogin.core.auth.response.HasJoinedResponse;

/**
 * 公共登入状态中的用户实例<br>
 * 需要在完成对称加密状态下使用此实例
 */
public abstract class BaseUserLogin {
    private final String username;
    private final String serverId;
    private final String ip;

    protected BaseUserLogin(String username, String serverId, String ip) {
        this.username = username;
        this.serverId = serverId;
        this.ip = ip;
    }

    /**
     * 拒绝并且踢出该名正在登入中的玩家
     *
     * @param message 踢出理由
     */
    public abstract void disconnect(String message);

    /**
     * 获得用户名
     *
     * @return 用户名
     */
    public final String getUsername() {
        return username;
    }

    /**
     * 获得服务器ID
     *
     * @return 服务器ID
     */
    public final String getServerId() {
        return serverId;
    }

    /**
     * 获得IP
     *
     * @return IP，如果拥有
     */
    public final String getIp() {
        return ip;
    }

    /**
     * 登入结束<br>
     * 此方法传入的 response 一定是有效的用户数据，不需要再次进行非空或有效判断
     */
    public abstract void finish(HasJoinedResponse response);
}
