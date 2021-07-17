package moe.caa.multilogin.core.language;

import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.logger.MultiLogger;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.util.YamlConfig;

import java.io.File;
import java.io.FileInputStream;
import java.text.MessageFormat;

/**
 * 可读消息处理程序
 */
public class LanguageHandler {
    private static final YamlConfig defaultLanguageYamlConfig;
    private static YamlConfig languageYamlConfig;
    private static boolean outside = false;

    static {
        defaultLanguageYamlConfig = YamlConfig.fromInputStream(MultiCore.plugin.getJarResource("language.yml"));
    }

    public static void init() {
        File languageFile = new File(MultiCore.plugin.getDataFolder(), "language.yml");
        if (languageFile.exists()) {
            try {
                languageYamlConfig = YamlConfig.fromInputStream(new FileInputStream(languageFile));
                outside = true;
            } catch (Exception ignore) {
                languageYamlConfig = null;
            }
        }
        MultiLogger.log(LoggerLevel.INFO, outside ? LanguageKeys.USE_OUTSIDE_LANGUAGE.getMessage() : LanguageKeys.USE_INSIDE_LANGUAGE.getMessage());
        if (outside) testLanguage();
    }

    private static void testLanguage() {
        for (LanguageKeys value : LanguageKeys.values()) {
            String msg = languageYamlConfig.get(value.key, String.class);
            if (msg != null) {
                try {
                    MessageFormat.format(msg, value.args);
                    continue;
                } catch (Exception ignored) {
                }
            }
            repairLanguagePath(value.key);
            MultiLogger.log(LoggerLevel.WARN, LanguageKeys.REPAIR_LANGUAGE_KEY.getMessage(value.key));
        }
    }

    private static void repairLanguagePath(String path) {
        languageYamlConfig.set(path, defaultLanguageYamlConfig.get(path, String.class));
    }

    protected static String getMessage(LanguageKeys keys, Object... args) {
        return MessageFormat.format(outside ? languageYamlConfig.get(keys.key, String.class) : defaultLanguageYamlConfig.get(keys.key, String.class), args);
    }
}
