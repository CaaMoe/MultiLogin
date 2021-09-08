package moe.caa.multilogin.core.impl;

import moe.caa.multilogin.core.logger.LoggerLevel;

import java.io.File;
import java.util.logging.Level;

/**
 * 代表插件主类
 */
public interface IPlugin {

    /**
     * 修改原有的验证服务
     * @throws Throwable 异常
     */
    void initService() throws Throwable;

    /**
     * 加载其他附加服务
     */
    void initOther();

    /**
     * 获得服务器对象
     * @return 服务器对象
     */
    IServer getRunServer();

    /**
     * 写入一条日志<br>
     * 这是一个早期写入日志的方法，用于处理 Log4J Core 加载前的日志信息
     * @param level 日志级别
     * @param message 日志信息
     * @param throwable 爆栈信息
     */
    void loggerLog(LoggerLevel level, String message, Throwable throwable);

    /**
     * 获得配置和数据文件路径
     * @return 配置和数据文件路径
     */
    File getDataFolder();
}
