package moe.caa.multilogin.api.logging;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IMultiloginLogger {
    void log(@NotNull LogLevel level, @Nullable String message, @Nullable Throwable throwable);

    default void debug(@Nullable String message, @Nullable Throwable throwable) {
        log(LogLevel.DEBUG, message, throwable);
    }
    default void info(@Nullable String message, @Nullable Throwable throwable) {
        log(LogLevel.INFO, message, throwable);
    }
    default void warn(@Nullable String message, @Nullable Throwable throwable) {
        log(LogLevel.WARN, message, throwable);
    }
    default void error(@Nullable String message, @Nullable Throwable throwable) {
        log(LogLevel.ERROR, message, throwable);
    }

    default void debug(@Nullable String message) {
        log(LogLevel.DEBUG, message, null);
    }
    default void info(@Nullable String message) {
        log(LogLevel.INFO, message, null);
    }
    default void warn(@Nullable String message) {
        log(LogLevel.WARN, message, null);
    }
    default void error(@Nullable String message) {
        log(LogLevel.ERROR, message, null);
    }
}
