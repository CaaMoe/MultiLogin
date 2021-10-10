package moe.caa.multilogin.core.loader.impl;

import java.io.File;
import java.util.logging.Level;

/**
 * 子端加载器对象
 */
public interface ISectionLoader {

    /**
     * 写入一条日志
     *
     * @param level     日志级别
     * @param message   日志信息
     * @param throwable 爆栈信息
     */
    void loggerLog(Level level, String message, Throwable throwable);

    /**
     * 获得配置和数据文件路径
     *
     * @return 配置和数据文件路径
     */
    File getDataFolder();
}
