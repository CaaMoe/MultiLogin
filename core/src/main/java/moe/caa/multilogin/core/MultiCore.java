package moe.caa.multilogin.core;

import moe.caa.multilogin.core.auth.HttpAuth;
import moe.caa.multilogin.core.data.data.PluginData;
import moe.caa.multilogin.core.impl.IPlugin;
import moe.caa.multilogin.core.util.AutoUpdater;

/**
 * 插件核心类
 */
public class MultiCore {
    private static IPlugin plugin = null;

    /**
     * 启动服务
     *
     * @param plugin 插件对象
     * @return 是否加载成功
     */
    public static boolean initService(IPlugin plugin) {
        MultiCore.plugin = plugin;
//        自动更新
        plugin.runTaskAsyncTimer(AutoUpdater::update, 20 * 60 * 60 * 12, 20 * 60 * 60 * 12);
        plugin.runTaskAsyncLater(AutoUpdater::setUpUpdate, 0);
        try {
            PluginData.initService();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 关闭插件
     */
    public static void disable() {
        PluginData.close();
        HttpAuth.shutDown();
    }

    /**
     * 获得插件对象
     *
     * @return 当前插件对象
     */
    public static IPlugin getPlugin() {
        return plugin;
    }

    public static void info(String info) {
        plugin.getPluginLogger().info(info);
    }
}
