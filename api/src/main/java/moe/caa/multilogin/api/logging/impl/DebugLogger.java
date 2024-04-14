package moe.caa.multilogin.api.logging.impl;

import moe.caa.multilogin.api.logging.IMultiloginLogger;
import moe.caa.multilogin.api.logging.LogLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DebugLogger implements IMultiloginLogger {
    private final IMultiloginLogger innerLogger;

    public DebugLogger(IMultiloginLogger logger) {
        this.innerLogger = logger;
    }

    @Override
    public void log(@NotNull LogLevel level, @Nullable String message, @Nullable Throwable throwable) {
        if (level == LogLevel.DEBUG) {
            innerLogger.log(LogLevel.INFO, "[DEBUG] " + message, throwable);
        } else {
            innerLogger.log(level, message, throwable);
        }
    }
}
