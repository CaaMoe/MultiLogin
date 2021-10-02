package moe.caa.multilogin.core.impl;

/**
 * 公共登入状态中的用户实例
 */
public interface IUserLogin {

    /**
     * 拒绝并且踢出该名正在登入中的玩家
     *
     * @param message 踢出理由
     */
    void disconnect(String message);

    /**
     * 获得用户名
     *
     * @return 用户名
     */
    String getUsername();

    /**
     * 获得服务器ID
     *
     * @return 服务器ID
     */
    String getServerId();

    /**
     * 获得IP
     *
     * @return IP，如果拥有
     */
    String getIp();

    /**
     * 加密开始
     *
     * @return 服务器ID
     */
    String startEncrypting();

    /**
     * 登入结束
     */
    void finish();
}
