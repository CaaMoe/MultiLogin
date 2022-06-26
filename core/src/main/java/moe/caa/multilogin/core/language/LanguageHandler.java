package moe.caa.multilogin.core.language;

import moe.caa.multilogin.api.language.LanguageAPI;
import moe.caa.multilogin.api.logger.LoggerProvider;
import moe.caa.multilogin.api.util.Pair;
import moe.caa.multilogin.api.util.ValueUtil;
import moe.caa.multilogin.core.main.MultiCore;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Properties;

/**
 * 代表可读文本处理程序
 */
public class LanguageHandler implements LanguageAPI {
    private final MultiCore core;
    private Properties inside;
    private Properties outside;

    public LanguageHandler(MultiCore core) {
        this.core = core;
    }

    /**
     * 初始化这个可读文本处理程序
     */
    public void init() throws IOException {
        inside = new Properties();
        try (InputStream is = Objects.requireNonNull(getClass().getResourceAsStream("/message.properties"))) {
            inside.load(new InputStreamReader(is, StandardCharsets.UTF_8));
        }
        reloadOutside();
    }

    /**
     * 重新加载外置语言仓库
     */
    public void reloadOutside() {
        File outsideFile = new File(core.getPlugin().getDataFolder(), "message.properties");
        if (outsideFile.exists()) {
            outside = new Properties();
            try {
                outside.load(new InputStreamReader(new FileInputStream(outsideFile), StandardCharsets.UTF_8));
                LoggerProvider.getLogger().info("Use outside language properties.");
            } catch (IOException e) {
                LoggerProvider.getLogger().error(String.format("Unable to load outside message file. (%s)", outsideFile.getAbsolutePath()), e);
            }
        } else {
            outside = null;
        }
    }

    public final String getMessage(String node, Pair<?, ?>... pairs) {
        if (outside != null && outside.containsKey(node)) {
            return ValueUtil.transPapi(outside.getProperty(node), pairs);
        } else {
            return ValueUtil.transPapi(inside.getProperty(node), pairs);
        }
    }
}
