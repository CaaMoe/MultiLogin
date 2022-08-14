package moe.caa.multilogin.core.ohc;

import java.io.IOException;

/**
 * 代表线程休眠异常
 */
public class InterruptedRetryException extends IOException {

    public InterruptedRetryException() {
    }

    public InterruptedRetryException(String message) {
        super(message);
    }

    public InterruptedRetryException(String message, Throwable cause) {
        super(message, cause);
    }

    public InterruptedRetryException(Throwable cause) {
        super(cause);
    }
}
