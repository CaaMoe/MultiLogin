package moe.caa.multilogin.core.configuration;

import java.io.IOException;

/**
 * 配置异常
 */
public class ConfException extends IOException {
    public ConfException() {
    }

    public ConfException(String message) {
        super(message);
    }

    public ConfException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfException(Throwable cause) {
        super(cause);
    }
}
