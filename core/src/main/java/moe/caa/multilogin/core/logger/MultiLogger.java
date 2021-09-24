package moe.caa.multilogin.core.logger;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import moe.caa.multilogin.core.main.MultiCore;

/**
 * 插件日志综合处理程序<br>
 * 处理日志文件保存和终端打印<br>
 * 需要后端实现提供控制台日志打印方法
 */
public class MultiLogger {
    @Getter
    private static MultiLogger logger;
    @Getter(value = AccessLevel.PROTECTED)
    private final MultiCore core;
    private FileLoggerWriteHandler flwh;
    @Getter
    @Setter
    private boolean debug = false;

    /**
     * 构建这个插件日志综合处理程序<br>
     * 在不执行 init 方法前，本模块仅能处理打印日志到控制台的功能
     *
     * @param core 插件核心
     */
    public MultiLogger(MultiCore core) {
        logger = this;
        this.core = core;
    }

    /**
     * 初始化操作
     *
     * @param debug 是否开启调试模式
     */
    public void init(boolean debug) {
        this.debug = debug;
        try {
            this.flwh = new FileLoggerWriteHandler();
            flwh.init();
        } catch (Throwable e) {
            log(LoggerLevel.ERROR, "Unable to save log to file.", e);
            log(LoggerLevel.ERROR, "debug will be enabled.");
            this.flwh = null;
            this.debug = true;
        }
        if (debug) log(LoggerLevel.WARN, "Debug mode.");
    }

    /**
     * 写入一条日志
     *
     * @param level   日志级别
     * @param message 日志信息
     */
    public void log(LoggerLevel level, String message) {
        log(level, message, null);
    }

    /**
     * 写入一条日志
     *
     * @param level     日志级别
     * @param message   日志信息
     * @param throwable 栈信息
     */
    public void log(LoggerLevel level, String message, Throwable throwable) {
        if (debug && level == LoggerLevel.DEBUG) {
            level = LoggerLevel.INFO;
        }
        core.getPlugin().loggerLog(level, message, throwable);
        if (flwh != null) flwh.log(level, message, throwable);
    }
}
