package moe.caa.multilogin.api.logging.impl;

import moe.caa.multilogin.api.logging.IMultiloginLogger;
import moe.caa.multilogin.api.logging.LogLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SysOutLogger implements IMultiloginLogger {
    @Override
    public void log(@NotNull LogLevel level, @Nullable String message, @Nullable Throwable throwable) {
        System.out.printf("[%1$s] %2$s\n", level, message);

        switch (level) {
            case DEBUG, INFO -> {
                if (throwable != null) {
                    throwable.printStackTrace(System.out);
                }
            }
            case WARN, ERROR -> {
                if (throwable != null) {
                    throwable.printStackTrace(System.err);
                }
            }
        }
    }
}
