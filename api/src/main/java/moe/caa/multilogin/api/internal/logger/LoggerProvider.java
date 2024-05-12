package moe.caa.multilogin.api.internal.logger;

import lombok.Getter;
import lombok.Setter;
import moe.caa.multilogin.api.internal.logger.bridges.ConsoleBridge;

/**
 * 日志提供程序
 */
public class LoggerProvider {
    @Getter
    @Setter
    private static Logger logger = new ConsoleBridge();
}
