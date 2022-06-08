package moe.caa.multilogin.core.language;

import moe.caa.multilogin.api.logger.LoggerProvider;
import moe.caa.multilogin.api.util.ValueUtil;
import moe.caa.multilogin.core.main.MultiCore;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * 代表可读文本处理程序
 */
public class LanguageHandler {
    private final MultiCore core;
    private Properties inside;
    private Properties outside;

    public LanguageHandler(MultiCore core) {
        this.core = core;
    }

    /**
     * 初始化这个可读文本处理程序
     */
    public void init(String fileName) throws IOException {
        inside = new Properties();
        try (InputStream is = Objects.requireNonNull(getClass().getResourceAsStream("/" + fileName))) {
            inside.load(new InputStreamReader(is, StandardCharsets.UTF_8));
        }
        reloadOutside(fileName);
    }

    /**
     * 重新加载外置语言仓库
     */
    public void reloadOutside(String fileName) {
        var outsideFile = new File(core.getPlugin().getDataFolder(), fileName);
        if (outsideFile.exists()) {
            outside = new Properties();
            try {
                outside.load(new InputStreamReader(new FileInputStream(outsideFile), StandardCharsets.UTF_8));
            } catch (IOException e) {
                LoggerProvider.getLogger().error(String.format("Unable to load outside message file. (%s)", outsideFile.getAbsolutePath()), e);
            }
        } else {
            outside = null;
        }
    }

    /**
     * 通过 节点 和 参数 构建这个可读文本字符串对象
     *
     * @param node 节点
     * @param papi 占位参数
     * @return 可读文本字符串对象
     */
    public String getMessage(String node, Map<String, Object> papi) {
        if (outside != null && outside.containsKey(node)) {
            return ValueUtil.transPapi(outside.getProperty(node), papi);
        } else {
            return ValueUtil.transPapi(inside.getProperty(node), papi);
        }
    }

    public String getMessage(String node) {
        return getMessage(node, Collections.emptyMap());
    }
}
