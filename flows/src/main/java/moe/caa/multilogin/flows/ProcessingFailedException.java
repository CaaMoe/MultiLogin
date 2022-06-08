package moe.caa.multilogin.flows;

/**
 * 加工异常
 */
public class ProcessingFailedException extends RuntimeException {
    public ProcessingFailedException(String message) {
        super(message);
    }

    public ProcessingFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProcessingFailedException(Throwable cause) {
        super(cause);
    }
}
