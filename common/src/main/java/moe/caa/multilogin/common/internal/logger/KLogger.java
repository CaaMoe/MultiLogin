package moe.caa.multilogin.common.internal.logger;

import org.slf4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;

public class KLogger {
    private final Logger handle;
    private boolean debugAsInfo = false;

    public KLogger(Logger handle) {
        this.handle = handle;
    }

    public boolean isDebugAsInfo() {
        return debugAsInfo;
    }

    public void setDebugAsInfo(boolean debugAsInfo) {
        this.debugAsInfo = debugAsInfo;
    }

    public void debug(String message, Throwable throwable) {
        if (debugAsInfo) {
            info(message, throwable);
        } else {
            handle.debug(message, throwable);
        }
    }

    public void info(String message, Throwable throwable) {
        handle.info(message, throwable);
    }

    public void warn(String message, Throwable throwable) {
        handle.warn(message, throwable);
    }

    public void error(String message, Throwable throwable) {
        handle.error(message, throwable);
    }

    public void debug(String message) {
        if (debugAsInfo) {
            info("[DEBUG] " + message);
        } else {
            handle.debug(message);
        }
    }

    public void info(String message) {
        handle.info(message);
    }

    public void warn(String message) {
        handle.warn(message);
    }

    public void error(String message) {
        handle.error(message);
    }

    public void checkLogAsDebugFlag(Path dataDirectory) {
        debugAsInfo = Files.exists(dataDirectory.resolve("debug"));
    }
}
