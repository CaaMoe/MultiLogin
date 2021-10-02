package moe.caa.multilogin.core.impl;

import moe.caa.multilogin.core.logger.LoggerLevel;

import java.io.File;

/**
 * 公共插件实例
 */
public interface IPlugin {

    /**
     * 调用此方法通知实现类进行替换原有的账户验证服务
     */
    void initService();

    /**
     * 调用此方法通知实现类加载除替换原有的账户验证服务外的其他附加服务<br>
     * 包括命令注册、变量注册
     */
    void initOther();

    /**
     * 写入一条日志
     *
     * @param level     日志级别
     * @param message   日志信息
     * @param throwable 爆栈信息
     */
    void loggerLog(LoggerLevel level, String message, Throwable throwable);

    /**
     * 获得配置和数据文件路径
     *
     * @return 配置和数据文件路径
     */
    File getDataFolder();
}
