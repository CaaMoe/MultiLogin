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
import moe.caa.multilogin.core.util.I18n;

/**
 * 插件核心类
 */
public class MultiCore {
    private static final AutoUpdater updater = new AutoUpdater();
    private static IPlugin plugin = null;

    /**
     * 启动服务
     *
     * @param plugin 插件对象
     * @return 是否加载成功
     */
    public static boolean initService(IPlugin plugin) {
        MultiCore.plugin = plugin;
        I18n.initService();
        try {
            LibChecker libChecker = new LibChecker(plugin.getPluginDataFolder());
            if (!libChecker.check()) {
                severe(I18n.getTransString("plugin_error_loading_library"));
                return false;
            }
            PluginData.initService();
        } catch (Exception e) {
            e.printStackTrace();
        }
        plugin.runTaskAsyncTimer(updater, 20 * 60 * 60 * 12, 20 * 60 * 60 * 12);
        plugin.runTaskAsyncLater(updater::infoUpdate, 0);
        return true;
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

    public static void severe(String info) {
        plugin.getPluginLogger().severe(info);
    }

    public static boolean isUpdate() {
        return updater.isUpdate();
    }
}
