package moe.caa.multilogin.core.impl;

import lombok.Getter;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 代表一个登入状态中的用户实例
 */
@Getter
public abstract class AbstractUserLogin {
    private static final AtomicInteger nextLoginId = new AtomicInteger(0);
    private final int loginId;

    protected AbstractUserLogin() {
        this.loginId = getNextId();
    }

    /**
     * 获得下一登入ID
     *
     * @return 登入ID
     */
    private static int getNextId() {
        return nextLoginId.incrementAndGet();
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
    public abstract String getUsername();

    /**
     * 获得服务器ID
     *
     * @return 服务器ID
     */
    public abstract String getServerId();

    /**
     * 获得IP
     *
     * @return IP，如果拥有
     */
    public abstract String getIp();
}
