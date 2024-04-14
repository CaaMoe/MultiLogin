package moe.caa.multilogin.api.logging.impl;

import moe.caa.multilogin.api.logging.IMultiloginLogger;
import moe.caa.multilogin.api.logging.LogLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NullLogger implements IMultiloginLogger {
    @Override
    public void log(@NotNull LogLevel level, @Nullable String message, @Nullable Throwable throwable) {
        // Do nothing.
    }
}
