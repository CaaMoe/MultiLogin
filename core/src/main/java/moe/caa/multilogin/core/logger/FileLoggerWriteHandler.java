package moe.caa.multilogin.core.logger;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.var;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.util.IOUtil;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * 日志文件写入程序，使用 log4j<br>
 * 此实例仅能通过 MultiLogger 来访问<br>
 * 请确保当前类或父加载器包含 log4j 库<br>
 *
 * @see MultiLogger
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FileLoggerWriteHandler {
    private Logger toFileLogger;

    /**
     * 初始化这个日志记录程序
     *
     * @param core 插件核心
     * @throws IOException 文件存取异常
     */
    protected void init(MultiCore core) throws IOException {
        var tempFile = File.createTempFile("log4j2-temp", "multilogin");
        tempFile.deleteOnExit();
        var reader = new LineNumberReader(new InputStreamReader(Objects.requireNonNull(IOUtil.getJarResource("multilogin_log4j2.xml"), String.format("File '%s' was not found in the jar.", "multilogin_log4j2.xml"))));
        var rePlacePath = core.getPlugin().getDataFolder().getAbsolutePath();
        try (var fw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(tempFile), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                fw.write(line.replace("%path%", rePlacePath));
                fw.write('\n');
            }
            fw.flush();
        }
        var context = new LoggerContext("MultiLogin_To_Logfile");
        context.setConfigLocation(tempFile.toURI());
        context.reconfigure();
        toFileLogger = context.getLogger("MultiLogin_To_Logfile");
    }

    /**
     * 写入一条日志到文件中
     *
     * @param level     日志级别
     * @param message   日志信息
     * @param throwable 栈信息
     */
    protected void log(LoggerLevel level, String message, Throwable throwable) {
        Level log4Level;
        if (level == LoggerLevel.INFO) log4Level = Level.INFO;
        else if (level == LoggerLevel.WARN) log4Level = Level.WARN;
        else if (level == LoggerLevel.ERROR) log4Level = Level.ERROR;
        else if (level == LoggerLevel.DEBUG) log4Level = Level.DEBUG;
        else log4Level = Level.INFO;
        log(log4Level, message, throwable);
    }

    /**
     * 写入一条日志到文件中
     *
     * @param level     日志级别
     * @param message   日志信息
     * @param throwable 栈信息
     */
    protected void log(Level level, String message, Throwable throwable) {
        if (toFileLogger != null) toFileLogger.log(level, message, throwable);
    }
}
