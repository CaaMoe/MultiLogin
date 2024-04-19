package moe.caa.multilogin.api.exception;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class BreakSignalException extends RuntimeException {
    public BreakSignalException() {
    }

    public BreakSignalException(String message) {
        super(message);
    }
}
