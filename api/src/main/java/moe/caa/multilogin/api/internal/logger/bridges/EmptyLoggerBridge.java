package moe.caa.multilogin.api.internal.logger.bridges;

import lombok.NoArgsConstructor;
import moe.caa.multilogin.api.internal.logger.Level;
import org.jetbrains.annotations.ApiStatus;

/**
 * 空日志程序桥接
 */
@ApiStatus.Internal
@NoArgsConstructor
public class EmptyLoggerBridge extends BaseLoggerBridge {
    @Override
    public void log(Level level, String message, Throwable throwable) {

    }
}
