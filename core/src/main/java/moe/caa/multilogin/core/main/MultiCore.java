/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * moe.caa.multilogin.core.main.MultiCore
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package moe.caa.multilogin.core.main;

import moe.caa.multilogin.core.auth.AuthCore;
import moe.caa.multilogin.core.auth.Verifier;
import moe.caa.multilogin.core.command.CommandHandler;
import moe.caa.multilogin.core.data.database.SQLManager;
import moe.caa.multilogin.core.impl.AbstractScheduler;
import moe.caa.multilogin.core.impl.IPlugin;
import moe.caa.multilogin.core.language.LanguageHandler;
import moe.caa.multilogin.core.language.LanguageKeys;
import moe.caa.multilogin.core.library.LibraryHandler;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.logger.MultiLogger;
import moe.caa.multilogin.core.util.FileUtil;
import moe.caa.multilogin.core.util.ReflectUtil;
import moe.caa.multilogin.core.util.YamlConfig;
import moe.caa.multilogin.core.yggdrasil.YggdrasilServicesHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 核心类
 */
public class MultiCore {
    //    ksqeib
    private static MultiCore instance;

    private final IPlugin plugin;
    private final Verifier verifier = new Verifier();
    private final CheckUpdater updater = new CheckUpdater();
    private final CommandHandler commandHandler = new CommandHandler();
    private final SQLManager sqlManager = new SQLManager();
    private final YggdrasilServicesHandler yggdrasilServicesHandler = new YggdrasilServicesHandler();
    private final MultiLogger logger = new MultiLogger();
    private final LanguageHandler languageHandler = new LanguageHandler();
    private final AuthCore authCore = new AuthCore();
    public YamlConfig config = null;
    public List<String> safeId = new ArrayList<>();
    public int servicesTimeOut = 10000;
    public boolean whitelist = true;
    public String nameAllowedRegular = "^[0-9a-zA-Z_]{1,16}$";
    private MetricsLite metricsLite;
    private File configFile;

    public MultiCore(IPlugin plugin) {
        this.plugin = plugin;
        instance = this;
    }

    //    ksqeib 2021-7-26
    public static MultiCore getInstance() {
        return instance;
    }

    public static AbstractScheduler getScheduler() {
        return getInstance().plugin.getSchedule();
    }

    public static void log(LoggerLevel info, Throwable throwable) {
        log(info, null, throwable);
    }

    public static void log(LoggerLevel info, String message) {
        log(info, message, null);
    }

    public static void log(LoggerLevel level, String message, Throwable throwable) {
        getInstance().getLogger().log(level, message, throwable);
    }

    public static YamlConfig getConfig() {
        return getInstance().config;
    }

    public static IPlugin getPlugin() {
        return getInstance().plugin;
    }

    public boolean init() {
        try {
            LibraryHandler libraryHandler = new LibraryHandler(this);
            configFile = new File(plugin.getDataFolder(), "config.yml");

            try {
                ReflectUtil.init();
            } catch (Exception e) {
                logger.log(LoggerLevel.ERROR, e);
                RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
                String java = runtime == null ? "unknown" : MessageFormat.format("Java {0} ({1} {2})", runtime.getSpecVersion(), runtime.getVmName(), runtime.getVmVersion());
//                logger.log(LoggerLevel.ERROR, LanguageKeys.REFLECT_INIT_ERROR.getMessage(this, java));
                logger.log(LoggerLevel.ERROR, "ReflectUtil init error:" + java);
                return false;
            }

            try {
                libraryHandler.preInit();
            } catch (Exception e) {
                logger.log(LoggerLevel.ERROR, e);
                logger.log(LoggerLevel.ERROR, e.getMessage());
                return false;
            }
            languageHandler.init();

            try {
                genFile();
                config = YamlConfig.fromInputStream(new FileInputStream(configFile));
            } catch (IOException e) {
                logger.log(LoggerLevel.ERROR, e);
                logger.log(LoggerLevel.ERROR, LanguageKeys.CONFIG_LOAD_ERROR.getMessage(this));
                return false;
            }

            readConfig();

            try {
                libraryHandler.init();
            } catch (Throwable e) {
                logger.log(LoggerLevel.ERROR, e);
                logger.log(LoggerLevel.ERROR, e.getMessage());
                return false;
            }

            logger.init();

            yggdrasilServicesHandler.init();

            try {
                sqlManager.init();
            } catch (Exception e) {
                logger.log(LoggerLevel.ERROR, e);
                logger.log(LoggerLevel.ERROR, LanguageKeys.DATABASE_CONNECT_ERROR.getMessage(this));
                return false;
            }

            if (!plugin.isOnlineMode()) {
                logger.log(LoggerLevel.ERROR, LanguageKeys.NIT_ONLINE.getMessage(this));
                return false;
            }

            try {
                plugin.initCoreService();
            } catch (Throwable e) {
                logger.log(LoggerLevel.ERROR, e);
                logger.log(LoggerLevel.ERROR, LanguageKeys.ERROR_REDIRECT_MODIFY.getMessage(this));
                return false;
            }


            plugin.getSchedule().runTaskAsyncTimer(updater::check, 0, 1000 * 60 * 60 * 24);

            plugin.initOtherService();

            try {
//                最后加载bstats
                metricsLite = new MetricsLite(plugin);
            } catch (Exception e) {
                logger.log(LoggerLevel.ERROR, e);
                logger.log(LoggerLevel.ERROR, LanguageKeys.BSTATS_LOAD_FAIL.getMessage(this));
            }

            logger.log(LoggerLevel.INFO, LanguageKeys.PLUGIN_LOADED.getMessage(this));
            return true;
        } catch (Throwable e) {
            logger.log(LoggerLevel.ERROR, e);
            logger.log(LoggerLevel.ERROR, LanguageKeys.PLUGIN_LOAD_ERROR.getMessage(this));
            return false;
        }
    }

    /**
     * 读取配置文件
     */
    private void readConfig() {
        servicesTimeOut = config.get("servicesTimeOut", Number.class, 10000).intValue();
        List<?> list = config.get("safeId", List.class, Collections.emptyList());
        safeId.clear();
        for (Object o : list) {
            safeId.add(o.toString());
        }
        whitelist = config.get("whitelist", Boolean.class, true);
        nameAllowedRegular = config.get("nameAllowedRegular", String.class, "");
    }

    /**
     * 生成配置文件
     */
    private void genFile() throws IOException {
        FileUtil.createNewFileOrFolder(plugin.getDataFolder(), true);
        FileUtil.saveResource(plugin.getJarResource("config.yml"), configFile, false);
    }

    /**
     * 重新加载配置文件
     */
    public synchronized void reload() throws IOException {
        genFile();
        config = YamlConfig.fromInputStream(new FileInputStream(configFile));
        readConfig();
        yggdrasilServicesHandler.reload();
    }

    /**
     * 注销插件
     */
    public void disable() {
        sqlManager.close();
        plugin.shutdown();
        logger.log(LoggerLevel.INFO, LanguageKeys.PLUGIN_UNLOADED.getMessage(this));
    }

    public Verifier getVerifier() {
        return verifier;
    }

    public CheckUpdater getUpdater() {
        return updater;
    }

    public CommandHandler getCommandHandler() {
        return commandHandler;
    }

    public SQLManager getSqlManager() {
        return sqlManager;
    }

    public YggdrasilServicesHandler getYggdrasilServicesHandler() {
        return yggdrasilServicesHandler;
    }

    public MultiLogger getLogger() {
        return logger;
    }

    public LanguageHandler getLanguageHandler() {
        return languageHandler;
    }

    public AuthCore getAuthCore() {
        return authCore;
    }
}
