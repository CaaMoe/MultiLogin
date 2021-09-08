package moe.caa.multilogin.core.logger;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import moe.caa.multilogin.core.main.MultiCore;

/**
 * 插件日志综合处理程序
 */
public class MultiLogger {

    @Getter(value = AccessLevel.PROTECTED)
    private final MultiCore core;
    private FileLoggerWriteHandler flwh;

    @Getter
    @Setter
    private boolean debug = true;

    @Getter
    private static MultiLogger logger;

    /**
     * 构建这个插件日志综合处理程序
     * @param core 插件核心
     */
    public MultiLogger(MultiCore core) {
        logger = this;
        this.core = core;
    }

    public void init(boolean debug){
        this.debug = debug;
        try {
            this.flwh = new FileLoggerWriteHandler(this);
            flwh.init();
        } catch (Throwable e) {
            log(LoggerLevel.ERROR, "Unable to load logger saving service.", e);
            log(LoggerLevel.ERROR, "debug will be enabled.");
            this.flwh = null;
            this.debug = true;
        }
        if(debug) log(LoggerLevel.WARN, "Debug mode.");
    }

    /**
     * 写入一条日志
     * @param level 日志级别
     * @param message 日志信息
     */
    public void log(LoggerLevel level, String message){
        log(level, message, null);
    }

    /**
     * 写入一条日志
     * @param level 日志级别
     * @param message 日志信息
     * @param throwable 爆栈信息
     */
    public void log(LoggerLevel level, String message, Throwable throwable){
        if (debug && level == LoggerLevel.DEBUG) {
            level = LoggerLevel.INFO;
        }
        core.getPlugin().loggerLog(level, message, throwable);
        if(flwh != null) flwh.log(level, message, throwable);
    }
}
