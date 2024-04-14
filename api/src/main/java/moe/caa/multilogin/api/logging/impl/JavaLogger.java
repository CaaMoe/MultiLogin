package moe.caa.multilogin.api.logging.impl;

import moe.caa.multilogin.api.logging.IMultiloginLogger;
import moe.caa.multilogin.api.logging.LogLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;
import java.util.logging.Logger;

public class JavaLogger implements IMultiloginLogger {
    private final Logger logger;

    public JavaLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void log(@NotNull LogLevel level, @Nullable String message, @Nullable Throwable throwable) {
        switch (level) {
            case DEBUG -> logger.log(Level.FINE, message, throwable);
            case INFO -> logger.log(Level.INFO, message, throwable);
            case WARN -> logger.log(Level.WARNING, message, throwable);
            case ERROR -> logger.log(Level.SEVERE, message, throwable);
        }
    }
}
