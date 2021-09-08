package moe.caa.multilogin.core.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.var;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.logger.MultiLogger;
import moe.caa.multilogin.core.main.MultiCore;
import org.apache.logging.log4j.Level;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

/**
 * 流操作工具类
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IOUtil {

    /**
     * 获得插件Jar包文件流
     *
     * @param path Jar包文件路径
     * @return 对应的文件流
     */
    public static InputStream getJarResource(String path) {
        try {
            var url = MultiCore.class.getClassLoader().getResource(path);
            if (url != null) {
                var connection = url.openConnection();
                connection.setUseCaches(false);
                return connection.getInputStream();
            }
        } catch (IOException e) {
            MultiLogger.getLogger().log(LoggerLevel.ERROR, String.format("Unable to read the file stream in the jar. (%s)", path), e);
        }
        MultiLogger.getLogger().log(LoggerLevel.DEBUG, String.format("File not found in the jar. (%s)", path));
        return null;
    }

    /**
     * 保存流为文件
     *
     * @param input 输入流
     * @param file  目标文件
     * @param cover 是否覆盖
     */
    public static void saveResource(InputStream input, File file, boolean cover) throws IOException {
        if (file.exists() && !cover) return;
        try (var fOut = new FileOutputStream(file, false)) {
            var buf = new byte[1024];
            int len;
            while ((len = input.read(buf)) > 0) {
                fOut.write(buf, 0, len);
            }
            fOut.flush();
        }
    }

    /**
     * 清空文件内容
     *
     * @param file 源文件
     */
    public static void clearFile(File file) throws IOException {
        createNewFileOrFolder(file, false);
        var fileWriter = new FileWriter(file);
        fileWriter.write("");
        fileWriter.flush();
        fileWriter.close();
    }

    /**
     * 创建一个空文件或文件夹
     *
     * @param target 目标文件
     * @param folder 是否是文件夹
     * @return 文件是否存在
     */
    public static boolean createNewFileOrFolder(File target, boolean folder) throws IOException {
        if (target.exists())
            return true;
        if (folder) {
            return target.mkdirs();
        } else {
            return target.createNewFile();
        }
    }
}
