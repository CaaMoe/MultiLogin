package moe.caa.multilogin.core.language;

import lombok.NoArgsConstructor;
import lombok.var;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.logger.MultiLogger;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.util.IOUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Properties;

/**
 * 代表可读文本处理程序
 */
@NoArgsConstructor
public class LanguageHandler {
    private Properties inside;
    private Properties outside;

    /**
     * 初始化这个可读文本处理程序
     *
     * @param core     插件核心
     * @param fileName 可读文本文件名称
     */
    public boolean init(MultiCore core, String fileName) {
        inside = new Properties();
        try {
            inside.load(new InputStreamReader(IOUtil.getJarResource(fileName), StandardCharsets.UTF_8));
        } catch (Exception e) {
            MultiLogger.getLogger().log(LoggerLevel.ERROR, String.format("Unable to load inside message file. (%s)", fileName), e);
            return false;
        }
        var outsideFile = new File(core.getPlugin().getDataFolder(), fileName);
        if (outsideFile.exists()) {
            outside = new Properties();
            try {
                outside.load(new InputStreamReader(new FileInputStream(outsideFile), StandardCharsets.UTF_8));
            } catch (IOException e) {
                MultiLogger.getLogger().log(LoggerLevel.ERROR, String.format("Unable to load outside message file. (%s)", outsideFile.getAbsolutePath()), e);
            }
        } else {
            outside = null;
        }
        return true;
    }

    /**
     * 通过 节点 和 参数 构建这个可读文本字符串对象
     *
     * @param node 节点
     * @param args 占位参数
     * @return 可读文本字符串对象
     */
    public String getMessage(String node, Object... args) {
        var ret = String.format("The language file node '%s' is missing or wrong, please contact the administrator.", node);
        try {
            String pat;
            if (outside != null && outside.containsKey(node)) {
                pat = outside.getProperty(node);
            } else {
                pat = inside.getProperty(node);
            }
            ret = args.length == 0 ? pat : MessageFormat.format(pat, args);
        } catch (Exception e) {
            MultiLogger.getLogger().log(LoggerLevel.ERROR, String.format("The language file node '%s' is missing or wrong.", node), e);
        }
        return ret;
    }
}
