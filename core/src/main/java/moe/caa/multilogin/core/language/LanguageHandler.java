package moe.caa.multilogin.core.language;

import moe.caa.multilogin.api.internal.language.LanguageAPI;
import moe.caa.multilogin.api.internal.logger.LoggerProvider;
import moe.caa.multilogin.api.internal.util.IOUtil;
import moe.caa.multilogin.api.internal.util.Pair;
import moe.caa.multilogin.api.internal.util.ValueUtil;
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
        reload();
    }

    /**
     * 重新加载外置语言仓库
     */
    public final String getMessage(String node, Pair<?, ?>... pairs) {
        return ValueUtil.transPapi(language.getProperty(node), pairs);
    }

    public void reload() throws IOException {
        Properties tmp = new Properties();
        // 加载文件
        final File messagePropertiesFile = new File(core.getPlugin().getDataFolder(), "message.properties");
        if (!messagePropertiesFile.exists()) {
            try (OutputStream outputStream = new FileOutputStream(messagePropertiesFile);
                 InputStream resourceAsStream = Objects.requireNonNull(getClass().getResourceAsStream("/message.properties"))
            ) {
                IOUtil.copy(resourceAsStream, outputStream);
            }
            LoggerProvider.getLogger().info("Extract: message.properties");
        }

        // 加载文件内容
        try (InputStream inputStream = new FileInputStream(messagePropertiesFile);) {
            tmp.load(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        }

        // 补全内容
        try (InputStream resourceAsStream = Objects.requireNonNull(getClass().getResourceAsStream("/message.properties"));
             InputStreamReader isr = new InputStreamReader(resourceAsStream, StandardCharsets.UTF_8);
        ) {
            Properties inside = new Properties();
            inside.load(isr);
            for (Map.Entry<Object, Object> entry : inside.entrySet()) {
                if (tmp.containsKey(entry.getKey())) continue;
                tmp.setProperty(entry.getKey().toString(), entry.getValue().toString());
                LoggerProvider.getLogger().warn("Missing message from node " + entry.getKey().toString());
            }
        }
        language = tmp;
    }
}
