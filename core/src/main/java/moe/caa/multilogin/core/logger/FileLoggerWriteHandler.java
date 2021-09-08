package moe.caa.multilogin.core.logger;

import lombok.var;
import moe.caa.multilogin.core.util.IOUtil;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 日志文件写入程序，使用 LOG4J
 */
public class FileLoggerWriteHandler {
    private final MultiLogger multiLogger;
    private Logger toFileLogger;

    /**
     * 构建这个日志文件写入程序
     * @param multiLogger 插件日志记录程序
     */
    protected FileLoggerWriteHandler(MultiLogger multiLogger) {
        this.multiLogger = multiLogger;
    }

    /**
     * 初始化这个日志记录程序
     * @throws IOException 文件存取异常
     */
    protected void init() throws IOException {
        var tempFile = File.createTempFile("log4j2-temp", "multilogin");
        tempFile.deleteOnExit();
        var inputStream = IOUtil.getJarResource("log4j2.xml");
        var bytes = new byte[inputStream.available()];
        inputStream.read(bytes);
        var config = new String(bytes);
        try (var fw = new FileWriter(tempFile)){
            fw.write(config.replace("%path%", multiLogger.getCore().getPlugin().getDataFolder().getAbsolutePath()));
            fw.flush();
        }
        var context = new LoggerContext("MultiLogin");
        context.setConfigLocation(tempFile.toURI());
        context.reconfigure();
        toFileLogger = context.getLogger("MultiLogin");
    }

    protected void log(LoggerLevel level, String message, Throwable throwable){
        Level log4Level;
        if(level == LoggerLevel.INFO) log4Level = Level.INFO;
        else if(level == LoggerLevel.WARN) log4Level = Level.WARN;
        else if(level == LoggerLevel.ERROR) log4Level = Level.ERROR;
        else if(level == LoggerLevel.DEBUG) log4Level = Level.DEBUG;
        else log4Level = Level.INFO;
        log(log4Level, message, throwable);
    }

    protected void log(Level level, String message, Throwable throwable){
        if(toFileLogger != null) toFileLogger.log(level, message, throwable);
    }
}
