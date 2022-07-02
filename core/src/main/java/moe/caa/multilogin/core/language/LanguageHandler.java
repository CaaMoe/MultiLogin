package moe.caa.multilogin.core.language;

import moe.caa.multilogin.api.language.LanguageAPI;
import moe.caa.multilogin.api.logger.LoggerProvider;
import moe.caa.multilogin.api.util.IOUtil;
import moe.caa.multilogin.api.util.Pair;
import moe.caa.multilogin.api.util.ValueUtil;
import moe.caa.multilogin.core.main.MultiCore;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * 代表可读文本处理程序
 */
public class LanguageHandler implements LanguageAPI {
    private final MultiCore core;
    private Properties language;

    public LanguageHandler(MultiCore core) {
        this.core = core;
    }

    /**
     * 初始化这个可读文本处理程序
     */
    public void init() throws IOException {
        language = new Properties();
        if (!new File(core.getPlugin().getDataFolder(), "message.properties").exists()) {

            LoggerProvider.getLogger().info("Loading default messages.");
            InputStream inputStream = getClass().getResourceAsStream("/message.properties");
            language.load(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

            LoggerProvider.getLogger().info("Extracting message.properties.");
            File languageFile = new File(core.getPlugin().getDataFolder(), "message.properties");
            OutputStream outputStream = new FileOutputStream(languageFile);
            outputStream.write(getClass().getResourceAsStream("/message.properties").readAllBytes());
            outputStream.flush();
            outputStream.close();
        } else {
            LoggerProvider.getLogger().info("Loading custom messages.");
            InputStream inputStream = new FileInputStream(new File(core.getPlugin().getDataFolder(), "message.properties"));
            language.load(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

        }
    }

    /**
     * 重新加载外置语言仓库
     */
    public final String getMessage(String node, Pair<?, ?>... pairs) {
        return ValueUtil.transPapi(language.getProperty(node), pairs);
    }
}
