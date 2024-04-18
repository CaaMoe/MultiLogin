package moe.caa.multilogin.api.logger;

import moe.caa.multilogin.api.logger.bridge.SysOutLogger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public final class LoggerProvider {
    @NotNull
    public static Logger logger = new SysOutLogger();
    public static boolean debugMode = false;
}
