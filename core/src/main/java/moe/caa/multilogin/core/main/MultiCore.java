package moe.caa.multilogin.core.main;

import moe.caa.multilogin.core.data.database.SQLHandler;
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
    public static IPlugin plugin = null;
    public static YamlConfig config = null;
    public static List<String> safeId = new ArrayList<>();
    public static int servicesTimeOut = 10000;
    public static boolean whitelist = true;
    public static String nameAllowedRegular = "^[0-9a-zA-Z_]{1,16}$";
    private static File configFile;

    public static boolean init(IPlugin plugin) {
        configFile = new File(plugin.getDataFolder(), "config.yml");
        MultiCore.plugin = plugin;

        try {
            genFile();
            config = YamlConfig.fromInputStream(new FileInputStream(configFile));
        } catch (IOException e) {
            MultiLogger.log(LoggerLevel.ERROR, e);
            MultiLogger.log(LoggerLevel.ERROR, LanguageKeys.CONFIG_LOAD_ERROR.getMessage());
            return false;
        }

        readConfig();

        try {
            ReflectUtil.init();
        } catch (Exception e) {
            MultiLogger.log(LoggerLevel.ERROR, e);
            RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
            String java = runtime == null ? "unknown" : MessageFormat.format("Java {0} ({1} {2})", runtime.getSpecVersion(), runtime.getVmName(), runtime.getVmVersion());
            MultiLogger.log(LoggerLevel.ERROR, LanguageKeys.REFLECT_INIT_ERROR.getMessage(java));
            return false;
        }

        try {
            LibraryHandler.init();
        } catch (Throwable e) {
            MultiLogger.log(LoggerLevel.ERROR, e);
            MultiLogger.log(LoggerLevel.ERROR, e.getMessage());
            return false;
        }

        MultiLogger.init();
        LanguageHandler.init();
        YggdrasilServicesHandler.init();

        try {
            SQLHandler.init();
        } catch (Exception e) {
            MultiLogger.log(LoggerLevel.ERROR, e);
            MultiLogger.log(LoggerLevel.ERROR, LanguageKeys.DATABASE_CONNECT_ERROR.getMessage());
            return false;
        }

        if (!plugin.isOnlineMode()) {
            MultiLogger.log(LoggerLevel.ERROR, LanguageKeys.NIT_ONLINE.getMessage());
            return false;
        }

        try {
            plugin.initCoreService();
        } catch (Throwable e) {
            MultiLogger.log(LoggerLevel.ERROR, e);
            MultiLogger.log(LoggerLevel.ERROR, LanguageKeys.ERROR_REDIRECT_MODIFY.getMessage());
            return false;
        }

        return true;
    }

    private static void readConfig() {
        servicesTimeOut = config.get("servicesTimeOut", Number.class, 10000).intValue();
        List<?> list = config.get("safeId", List.class, Collections.emptyList());
        safeId.clear();
        for (Object o : list) {
            safeId.add(o.toString());
        }
        whitelist = config.get("whitelist", Boolean.class, true);
        nameAllowedRegular = config.get("nameAllowedRegular", String.class, "");
    }

    private static void genFile() throws IOException {
        FileUtil.createNewFileOrFolder(plugin.getDataFolder(), true);
        FileUtil.saveResource(plugin.getJarResource("config.yml"), configFile, false);
    }

    public static void reload() throws IOException {
        genFile();
        config = YamlConfig.fromInputStream(new FileInputStream(configFile));
        readConfig();
        YggdrasilServicesHandler.reload();
    }

    public static void disable(){


        plugin.shutdown();
    }
}
