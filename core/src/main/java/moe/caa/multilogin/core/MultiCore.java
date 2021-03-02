/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * moe.caa.multilogin.core.MultiCore
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package moe.caa.multilogin.core;

import moe.caa.multilogin.core.auth.HttpAuth;
import moe.caa.multilogin.core.data.data.PluginData;
import moe.caa.multilogin.core.impl.IPlugin;
import moe.caa.multilogin.core.lib.LibChecker;
import moe.caa.multilogin.core.util.AutoUpdater;

/**
 * 插件核心类
 */
public class MultiCore {
    private static IPlugin plugin = null;
    private static AutoUpdater updater = new AutoUpdater();
    private static LibChecker libChecker;

    /**
     * 启动服务
     *
     * @param plugin 插件对象
     * @return 是否加载成功
     */
    public static boolean initService(IPlugin plugin) {
        MultiCore.plugin = plugin;
//        自动更新
        plugin.runTaskAsyncTimer(updater, 20 * 60 * 60 * 12, 20 * 60 * 60 * 12);
        plugin.runTaskAsyncLater(() -> updater.infoUpdate(), 0);
//        检测并加载lib后启动
        libChecker = new LibChecker(plugin.getPluginDataFolder());
        if (!libChecker.check()) {
            info("库加载失败");
            return false;
        }
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

    public static boolean isUpdate() {
        return updater.isUpdate();
    }
}
