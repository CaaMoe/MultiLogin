package moe.caa.multilogin.core.ohc;

import java.io.IOException;

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
