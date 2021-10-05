package moe.caa.multilogin.core.language;

import lombok.var;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.logger.MultiLogger;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.util.FormatContent;
import moe.caa.multilogin.core.util.IOUtil;
import moe.caa.multilogin.core.util.ValueUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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
     *
     * @param core     插件核心
     * @param fileName 可读文本文件名称
     */
    public boolean init(MultiCore core, String fileName) {

        inside = new Properties();
        try {
            inside.load(new InputStreamReader(Objects.requireNonNull(IOUtil.getJarResource(fileName)), StandardCharsets.UTF_8));
        } catch (Exception e) {
            MultiLogger.getLogger().log(LoggerLevel.ERROR, String.format("Unable to load inside message file. (%s)", fileName), e);
            return false;
        }
        reloadOutside(fileName);
        return true;
    }

    /**
     * 重新加载外置语言仓库
     */
    public void reloadOutside(String fileName) {
        var outsideFile = new File(core.getPlugin().getDataFolder(), fileName);
        if (outsideFile.exists()) {
            MultiLogger.getLogger().log(LoggerLevel.INFO, String.format("加载文件: %s", outsideFile.getName()));
            outside = new Properties();
            try {
                outside.load(new InputStreamReader(new FileInputStream(outsideFile), StandardCharsets.UTF_8));
            } catch (IOException e) {
                MultiLogger.getLogger().log(LoggerLevel.ERROR, String.format("Unable to load outside message file. (%s)", outsideFile.getAbsolutePath()), e);
            }
        } else {
            outside = null;
        }
    }

    /**
     * 通过 节点 和 参数 构建这个可读文本字符串对象
     *
     * @param node    节点
     * @param content 占位参数
     * @return 可读文本字符串对象
     */
    public String getMessage(String node, FormatContent content) {
        var ret = String.format("The language file node '%s' is missing or wrong, please contact the administrator.", node);
        try {
            String pat;
            if (outside != null && outside.containsKey(node)) {
                pat = outside.getProperty(node);
            } else {
                pat = inside.getProperty(node);
            }
            ret = ValueUtil.format(pat, content);
        } catch (Exception e) {
            MultiLogger.getLogger().log(LoggerLevel.WARN, String.format("The language file node '%s' is missing or wrong.", node), e);
        }
        return ret;
    }
}
