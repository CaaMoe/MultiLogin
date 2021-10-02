package moe.caa.multilogin.core.impl;

/**
 * 公共服务器对象
 */
public interface IServer {

    /**
     * 获得线程调度器对象
     *
     * @return 线程调度器对象
     */
    BaseScheduler getScheduler();

    /**
     * 获得玩家管理器
     *
     * @return 获得玩家管理器
     */
    IPlayerManager getPlayerManager();

    /**
     * 获得服务器核心名称
     *
     * @return 服务器核心名称
     */
    String getName();

    /**
     * 获得服务器版本
     *
     * @return 服务器版本
     */
    String getVersion();

    /**
     * 关闭服务器
     */
    void shutdown();
}
