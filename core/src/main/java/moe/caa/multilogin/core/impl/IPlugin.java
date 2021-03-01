package moe.caa.multilogin.core.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * 可被core识别的插件对象
 */
public interface IPlugin {

    /**
     * 获得插件数据文件夹
     *
     * @return 插件数据文件夹
     */
    File getPluginDataFolder();

    /**
     * 获得插件配置文件
     *
     * @return 插件配置文件
     */
    IConfiguration getPluginConfig();

    /**
     * 保存插件默认配置文件（不覆盖）
     */
    void savePluginDefaultConfig();

    /**
     * 重新读取插件配置文件
     */
    void reloadPluginConfig();

    /**
     * 保存插件配置文件
     */
    void savePluginConfig();

    /**
     * 通过指定输入流生成一个配置文件对象
     *
     * @param reader 指定输入流
     * @return 配置文件对象
     */
    IConfiguration yamlLoadConfiguration(InputStreamReader reader) throws IOException;

    /**
     * 获得jar包文件数据流
     *
     * @param path 路径
     * @return 文件数据流
     */
    InputStream getPluginResource(String path);

    /**
     * 将某名玩家踢出游戏
     *
     * @param uuid 玩家的uuid
     * @param msg  玩家的名字
     */
    void kickPlayer(UUID uuid, String msg);

    /**
     * 获得插件的日志对象
     *
     * @return 插件日志对象
     */
    Logger getPluginLogger();

    /**
     * 获得插件当前版本
     *
     * @return 插件当前版本
     */
    String getVersion();

    /**
     * 延迟执行一个异步任务
     *
     * @param run   run
     * @param delay 延迟
     */
    void runTaskAsyncLater(Runnable run, long delay);

    /**
     * 执行一个异步延迟Timer任务
     *
     * @param run   run
     * @param delay 延迟
     * @param per   循环
     */
    void runTaskAsyncTimer(Runnable run, long delay, long per);

    /**
     * 执行一个同步任务
     *
     * @param run   run
     * @param delay 延迟
     */
    void runTask(Runnable run, long delay);

    /**
     * 获得在线玩家列表
     *
     * @return 在线玩家列表
     */
    Map<UUID, String> getOnlineList();
}
