package moe.caa.multilogin.core.language;

import moe.caa.multilogin.api.language.LanguageAPI;
import moe.caa.multilogin.api.logger.LoggerProvider;
import moe.caa.multilogin.api.util.IOUtil;
import moe.caa.multilogin.api.util.Pair;
import moe.caa.multilogin.api.util.ValueUtil;
import moe.caa.multilogin.core.main.MultiCore;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
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
        final File messagePropertiesFile = new File(core.getPlugin().getDataFolder(), "message.properties");
        if (!messagePropertiesFile.exists()) {
            LoggerProvider.getLogger().info("Extracting message.properties.");
            try (OutputStream outputStream = new FileOutputStream(messagePropertiesFile);
                 InputStream resourceAsStream = Objects.requireNonNull(getClass().getResourceAsStream("/message.properties"))
            ) {
                IOUtil.copy(resourceAsStream, outputStream);
            }
        }

        InputStream inputStream = new FileInputStream(messagePropertiesFile);
        language.load(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

        try (InputStream resourceAsStream = Objects.requireNonNull(getClass().getResourceAsStream("/message.properties"))
        ) {
            Properties inside = new Properties();
            inside.load(resourceAsStream);
            for (Map.Entry<Object, Object> entry : inside.entrySet()) {
                if (language.containsKey(entry.getKey())) continue;
                language.setProperty(entry.getKey().toString(), entry.getValue().toString());
                LoggerProvider.getLogger().warn("Missing message from node " + entry.getKey().toString());
            }
        }
    }

    /**
     * 重新加载外置语言仓库
     */
    public final String getMessage(String node, Pair<?, ?>... pairs) {
        return ValueUtil.transPapi(language.getProperty(node), pairs);
    }
}
