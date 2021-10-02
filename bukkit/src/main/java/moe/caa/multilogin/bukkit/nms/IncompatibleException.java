package moe.caa.multilogin.bukkit.nms;

/**
 * 不兼容异常
 */
public class IncompatibleException extends RuntimeException {

    public IncompatibleException() {
    }

    public IncompatibleException(String message) {
        super(message);
    }

    public IncompatibleException(String message, Throwable cause) {
        super(message, cause);
    }

    public IncompatibleException(Throwable cause) {
        super(cause);
    }
}
