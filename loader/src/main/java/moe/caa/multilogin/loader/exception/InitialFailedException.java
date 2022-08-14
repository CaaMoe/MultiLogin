package moe.caa.multilogin.loader.exception;

/**
 * 加载初始化异常
 */
public class InitialFailedException extends RuntimeException {

    public InitialFailedException(String message) {
        super(message);
    }

    public InitialFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public InitialFailedException(Throwable cause) {
        super(cause);
    }
}
