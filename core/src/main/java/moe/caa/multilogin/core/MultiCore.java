package moe.caa.multilogin.core;

import moe.caa.multilogin.core.data.database.SQLHandler;
import moe.caa.multilogin.core.impl.IPlugin;
import moe.caa.multilogin.core.language.LanguageHandler;
import moe.caa.multilogin.core.language.LanguageKeys;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.logger.MultiLogger;
import moe.caa.multilogin.core.util.FileUtil;
import moe.caa.multilogin.core.util.YamlConfig;
import moe.caa.multilogin.core.yggdrasil.YggdrasilServicesHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MultiCore {
    private static File configFile;
    public static IPlugin plugin = null;
    public static YamlConfig config = null;

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

        MultiLogger.init();
        LanguageHandler.init();
        YggdrasilServicesHandler.init();

        try {
            SQLHandler.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private static void genFile() throws IOException {
        FileUtil.createNewFileOrFolder(plugin.getDataFolder(), true);
        FileUtil.saveResource(plugin.getJarResource("config.yml"), configFile, false);
    }
}
