/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * moe.caa.multilogin.core.data.data.PluginData
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package moe.caa.multilogin.core.data.data;

import moe.caa.multilogin.core.MultiCore;
import moe.caa.multilogin.core.data.ServerTypeEnum;
import moe.caa.multilogin.core.data.databse.SQLHandler;
import moe.caa.multilogin.core.data.databse.pool.AbstractConnectionPool;
import moe.caa.multilogin.core.data.databse.pool.H2ConnectionPool;
import moe.caa.multilogin.core.data.databse.pool.MysqlConnectionPool;
import moe.caa.multilogin.core.impl.IConfiguration;
import moe.caa.multilogin.core.util.I18n;

import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * 处理插件数据
 */
public class PluginData {
    private static final Set<YggdrasilServiceEntry> serviceSet = new HashSet<>();
    public static IConfiguration configurationConfig = null;
    private static IConfiguration defaultConfigurationConfig = null;
    private static boolean whitelist = true;

    /**
     * 初始化
     */
    public static void initService() throws Exception {
        genFile();
        reloadConfig();

        AbstractConnectionPool args = getSqlPool(configurationConfig.getConfigurationSection("sql"));

        try {
            SQLHandler.init(args);
        } catch (Exception e) {
            MultiCore.getPlugin().getPluginLogger().info(I18n.getTransString("plugin_error_loading_database"));
            throw e;
        }
        MultiCore.getPlugin().getPluginLogger().info(I18n.getTransString("plugin_connected_database"));
    }

    /**
     * 生成插件数据文件夹
     */
    private static void genFile() throws IOException {
        if (!MultiCore.getPlugin().getPluginDataFolder().exists() && !MultiCore.getPlugin().getPluginDataFolder().mkdirs()) {
            throw new IOException(I18n.getTransString("plugin_severe_io_directory_mkdirs", MultiCore.getPlugin().getPluginDataFolder().getPath()));
        }
        MultiCore.getPlugin().savePluginDefaultConfig();
    }

    /**
     * 重新加载配置和白名单文件
     */
    public static void reloadConfig() throws IOException {
        serviceSet.clear();
        MultiCore.getPlugin().reloadPluginConfig();

        defaultConfigurationConfig = MultiCore.getPlugin().yamlLoadConfiguration(new InputStreamReader(MultiCore.getPlugin().getPluginResource("config.yml")));
        configurationConfig = MultiCore.getPlugin().getPluginConfig();

        Logger log = MultiCore.getPlugin().getPluginLogger();
        IConfiguration services = configurationConfig.getConfigurationSection("services");
        if (services != null) {
            boolean enableMinecraft = false;
            for (String path : services.getKeys(false)) {
                try {
                    YggdrasilServiceEntry section = YggdrasilServiceEntry.fromYaml(path, services.getConfigurationSection(path));
                    if (!section.isEnable()) continue;
                    if(section.getServerType() == ServerTypeEnum.MINECRAFT){
                        if(enableMinecraft){
                            log.warning(I18n.getTransString("plugin_severe_invalid_yggdrasil_repeat_vanilla", path));
                            continue;
                        }
                        enableMinecraft = true;
                    }
                    serviceSet.add(section);
                } catch (Exception e){
                    log.severe(I18n.getTransString("plugin_severe_invalid_yggdrasil", path, e.getMessage()));
                }
            }
        }
        log.info(I18n.getTransString("plugin_loaded_multi_Yggdrasil", serviceSet.size()));

        whitelist = configurationConfig.getBoolean("whitelist", true);

        testMsg(log, "msgNoAdopt");
        testMsg(log, "msgNoChae");
        testMsg(log, "msgRushName");
        testMsg(log, "msgNoWhitelist");
        testMsg(log, "msgNoPermission");
        testMsg(log, "msgAddWhitelist", "null");
        testMsg(log, "msgAddWhitelistAlready", "null");
        testMsg(log, "msgDelWhitelistInGame");
        testMsg(log, "msgDelWhitelist", "null");
        testMsg(log, "msgDelWhitelistAlready", "null");
        testMsg(log, "msgOpenWhitelist");
        testMsg(log, "msgOpenWhitelistAlready");
        testMsg(log, "msgCloseWhitelist");
        testMsg(log, "msgCloseWhitelistAlready");
        testMsg(log, "msgYDQuery", "null", "null");
        testMsg(log, "msgYDQueryNoRel", "null");
        testMsg(log, "msgReload");
        testMsg(log, "msgNoPlayer");
        testMsg(log, "msgRushNameOnl");
    }

    /**
     * 测试文本消息是否正确，并且将不正确的文本消息设置为默认值
     */
    @SuppressWarnings("all")
    private static void testMsg(Logger log, String path, Object... args) {
        try {
            String.format(configurationConfig.getString(path), args);
        } catch (Exception ignore) {
            configurationConfig.set(path, defaultConfigurationConfig.getString(path));
            log.warning(I18n.getTransString("plugin_severe_invalid_config_key", path));
        }
    }

    /**
     * 获得设置拥有ID保护功能的Yggdrasil服务器的path
     *
     * @return 设置拥有ID保护功能的Yggdrasil服务器的path
     */
    public static String getSafeIdService() {
        return configurationConfig.getString("safeId", "");
    }

    /**
     * 获得Yggdrasil验证超时时间
     *
     * @return Yggdrasil验证超时时间
     */
    public static long getTimeOut() {
        return configurationConfig.getLong("servicesTimeOut", 7000);
    }

    public static boolean isOpenSkinRepair(){
        return configurationConfig.getBoolean("skinRepair", false);
    }

    /**
     * 获得当前所有Yggdrasil验证服务器
     *
     * @return 当前所有Yggdrasil验证服务器
     */
    public static Set<YggdrasilServiceEntry> getServiceSet() {
        return serviceSet;
    }

    /**
     * 获得是否开启全局白名单
     *
     * @return 是否开启全局白名单
     */
    public static boolean isWhitelist() {
        return whitelist;
    }

    /**
     * 设置是否开启全局白名单
     *
     * @param whitelist 是否开启全局白名单
     */
    public synchronized static void setWhitelist(boolean whitelist) {
        PluginData.whitelist = whitelist;
        MultiCore.getPlugin().savePluginConfig();
    }

    /**
     * 判断一个字符串是否为空
     * 版本类库中没有稳定的地址
     *
     * @param str 字符串
     * @return 是否为空
     */
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    /**
     * 通过Yggdrasil服务器的path检索Yggdrasil服务器对象
     *
     * @param path Yggdrasil服务器的path
     * @return 检索到的Yggdrasil服务器对象
     */
    public static YggdrasilServiceEntry getYggdrasilServerEntry(String path) {
        for (YggdrasilServiceEntry serviceEntry : serviceSet) {
            if (serviceEntry.getPath().equalsIgnoreCase(path)) {
                return serviceEntry;
            }
        }
        return null;
    }

    /**
     * 保存数据
     */
    public static void close() {
        try {
            SQLHandler.close();
        } catch (SQLException ignored) {
        }
        MultiCore.getPlugin().savePluginConfig();
    }

    /**
     * 通过configuration获得SQL链接
     *
     * @param configuration SQL配置
     * @return 链接参数
     */
    private static AbstractConnectionPool getSqlPool(IConfiguration configuration) {
        if (configuration == null) return null;
        String url;
        String userName = configuration.getString("username");
        String password = configuration.getString("password");
        String backend = configuration.getString("backend", "H2");
        if ("MYSQL".equalsIgnoreCase(backend)) {
//            ip port 数据库名
            url = "jdbc:mysql://%s:%s/%s?autoReconnect=true&useUnicode=true&amp&characterEncoding=UTF-8&useSSL=false";
            url = String.format(url, configuration.getString("ip"), configuration.getString("port"), configuration.getString("database"));
            return new MysqlConnectionPool(url, userName, password);
        } else if ("H2".equalsIgnoreCase(backend)) {
//            位置 库名
            url = "jdbc:h2:%s%s;TRACE_LEVEL_FILE=0;TRACE_LEVEL_SYSTEM_OUT=0";
            url = String.format(url, MultiCore.getPlugin().getPluginDataFolder().getAbsolutePath(), "/multilogin");
            return new H2ConnectionPool(url, userName, password);
        } else {
//            不支持
            return null;
        }
    }
}
